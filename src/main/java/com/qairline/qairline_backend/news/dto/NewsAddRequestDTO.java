package com.qairline.qairline_backend.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class NewsAddRequestDTO {
    private String title;
    private String content;
    private String folder;
    private String imageUrl;
    private String classification;
}
