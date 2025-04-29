package com.freepath.devpath.email.query.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class NewsDto {
    private int itNewsId;
    private String title;
    private String link;
    private String content;
    private Date mailingDate;
}
