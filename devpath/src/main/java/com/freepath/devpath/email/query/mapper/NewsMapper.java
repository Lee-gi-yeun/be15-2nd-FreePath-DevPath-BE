package com.freepath.devpath.email.query.mapper;

import com.freepath.devpath.email.query.dto.NewsDto;
import com.freepath.devpath.email.query.dto.NewsSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NewsMapper {

    @Select("SELECT it_news_id, it_news_title, it_news_link, it_news_contents, mailing_date FROM it_news WHERE it_news_id = #{newsId}")
    NewsDto findNewsById(int newsId);

    List<NewsDto> selectNews(NewsSearchRequest newsSearchRequest);

    long countProducts(NewsSearchRequest newsSearchRequest);
}