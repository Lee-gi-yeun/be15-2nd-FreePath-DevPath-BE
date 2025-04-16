package com.freepath.devpath.email.command.application.service;

import com.freepath.devpath.command.client.UserClient;
import com.freepath.devpath.command.client.UserEmail;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.email.command.application.Dto.NewsRequestDto;
import com.freepath.devpath.email.command.domain.domain.News;
import com.freepath.devpath.email.command.domain.repository.NewsRepository;
import com.freepath.devpath.email.exception.NewsNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final JavaMailSender mailSender;
    private final UserClient userClient;

    // 뉴스 저장만
    public News saveNews(NewsRequestDto dto) {
        News news = News.builder()
                .title(dto.getTitle())
                .link(dto.getLink())
                .content(dto.getContent())
                .mailingDate(dto.getMailingDate())
                .build();
        newsRepository.save(news);
        return news;
    }

    // 특정 뉴스 ID로 이메일 발송
    public void sendNewsToSubscribers(int newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException(ErrorCode.NEWS_NOT_FOUND));

        List<UserEmail> subscribers = userClient.getSubscribedUsers();

        for (UserEmail user : subscribers) {
            sendEmail(user.getEmail(), news.getTitle(), news.getContent(),news.getLink());
        }

        // 뉴스에 발송 시간 기록
        news.setMailingDate(new Date());
        newsRepository.save(news);
    }

    private void sendEmail(String to, String subject, String content, String link) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[DevPath 뉴스] " + subject);

            String htmlContent = content.replace("\n","<br>") + "<br><br><a href='" + link + "'>👉 기사 전체 보기</a>";
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace(); // 또는 로그 처리
        }
    }

    public void sendNewsForToday() {
        List<News> todayNews = newsRepository.findNewsForToday();

        for (News news : todayNews) {
            sendNewsToSubscribers(news.getItNewsId()); // 기존 발송 메서드 재사용
        }
    }

    // 뉴스 수정
    public void updateNews(int newsId, NewsRequestDto dto) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException(ErrorCode.NEWS_NOT_FOUND));

        news.update(dto.getTitle(), dto.getLink(), dto.getContent(), dto.getMailingDate());
        newsRepository.save(news);
    }

    // 뉴스 삭제
    public void deleteNews(int newsId) {
        if (!newsRepository.existsById(newsId)) {
            throw new NewsNotFoundException(ErrorCode.NEWS_NOT_FOUND);
        }
        newsRepository.deleteById(newsId);
    }
}
