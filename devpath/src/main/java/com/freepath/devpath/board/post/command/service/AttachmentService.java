package com.freepath.devpath.board.post.command.service;

import com.freepath.devpath.board.post.command.domain.Attachment;
import com.freepath.devpath.board.post.command.exception.FileDeleteFailedException;
import com.freepath.devpath.board.post.command.repository.AttachmentRepository;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AttachmentService {

    private final S3Service s3Service;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public void uploadAndSaveFiles(List<MultipartFile> files, int boardId, int userId) {
        if (files == null || files.isEmpty()) return;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String s3Key = userId + "/" + uuid + "/" + originalFilename;

            try (InputStream is = file.getInputStream()) {
                String s3Url = s3Service.uploadFile(s3Key, is, file.getContentType());

                Attachment attachment = Attachment.builder()
                        .boardId(boardId)
                        .s3_key(s3Key)
                        .s3_url(s3Url)
                        .build();
                attachmentRepository.save(attachment);

            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패: " + originalFilename, e);
            }
        }
    }

    @Transactional
    public Map<String, String> uploadTempImage(MultipartFile image, int userId) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        String originalFilename = image.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String s3Key = "temp/" + userId + "/" + uuid + "/" + originalFilename;

        try (InputStream is = image.getInputStream()) {
            String s3Url = s3Service.uploadFile(s3Key, is, image.getContentType());

            Map<String, String> result = new HashMap<>();
            result.put("url", s3Url);
            result.put("key", s3Key);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("임시 이미지 업로드 실패: " + originalFilename, e);
        }
    }

    @Transactional
    public void migrateTempImages(List<String> imageUrls, int boardId, int userId) {
        for (String url : imageUrls) {
            String sourceKey = extractKeyFromUrl(url); // temp/123/.../img.png
            String filename = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
            String uuid = UUID.randomUUID().toString();
            String targetKey = "post/" + boardId + "/" + uuid + "/" + filename;

            try {
                // 복사 + 삭제
                s3Service.copyObject(sourceKey, targetKey);
                s3Service.deleteFile(sourceKey);

                String newUrl = s3Service.getFileUrl(targetKey);

                // DB 저장
                Attachment attachment = Attachment.builder()
                        .boardId(boardId)
                        .s3_key(targetKey)
                        .s3_url(newUrl)
                        .build();
                attachmentRepository.save(attachment);

            } catch (Exception e) {
                throw new RuntimeException("임시 이미지 이동 실패: " + sourceKey, e);
            }
        }
    }

    @Transactional
    public void deleteAttachmentsByBoardId(int boardId) {
        List<Attachment> attachments = attachmentRepository.findByBoardId(boardId);

        for (Attachment attachment : attachments) {
            try {
                s3Service.deleteFile(attachment.getS3_key());
                attachmentRepository.delete(attachment);
            } catch (Exception e) {
                throw new FileDeleteFailedException(ErrorCode.FILE_DELETE_FAILED);
            }
        }
    }

    private String extractKeyFromUrl(String url) {
        int index = url.indexOf(".amazonaws.com/");
        if (index == -1) {
            throw new IllegalArgumentException("S3 URL 형식이 잘못되었습니다: " + url);
        }

        return url.substring(index + ".amazonaws.com/".length());
    }

}
