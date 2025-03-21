package com.qairline.qairline_backend.news.model;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("news")
public class News {
    @Id
    private String id;

    @CreatedBy
    private String createdAdmin;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createdTime;

    private String title;
    private String imageUrl;
    private String content;
    private String folder;
    private String classification;
}
