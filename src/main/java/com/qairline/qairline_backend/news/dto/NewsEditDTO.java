package com.qairline.qairline_backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsEditDTO {
    private String id;
    private String title;
    private String content;
    private String folder;
    private String imageUrl;
    private String classification;
}
