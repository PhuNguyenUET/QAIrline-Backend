package com.qairline.qairline_backend.news.service;

import com.qairline.qairline_backend.news.dto.NewsAddRequestDTO;
import com.qairline.qairline_backend.news.dto.NewsEditDTO;
import com.qairline.qairline_backend.news.model.News;

import java.util.List;

public interface NewsService {
    void addNews(NewsAddRequestDTO dto);
    void editNews(NewsEditDTO dto);
    void deleteNews(String newsId);
    List<String> getAllClassifications();
    List<String> getAllFolders();
    News getNews(String newsId);
    List<News> getAllNews();
    List<News> getNewsFilter(String classification, String folder, String keyword);
}
