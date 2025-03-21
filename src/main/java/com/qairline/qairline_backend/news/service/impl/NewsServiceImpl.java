package com.qairline.qairline_backend.news.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.news.dto.NewsAddRequestDTO;
import com.qairline.qairline_backend.news.dto.NewsEditDTO;
import com.qairline.qairline_backend.news.model.Classification;
import com.qairline.qairline_backend.news.model.News;
import com.qairline.qairline_backend.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final MongoTemplate mongoTemplate;

    private final Set<String> folders = new HashSet<>();

    private final Cache<String, News> newsCache =
            CacheBuilder.newBuilder()
                    .maximumSize(500)
                    .expireAfterAccess(45, TimeUnit.MINUTES)
                    .build();


    @Override
    public void addNews(NewsAddRequestDTO dto) {
        News news = News.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .folder(dto.getFolder())
                .imageUrl(dto.getImageUrl())
                .classification(dto.getClassification() == null ? Classification.COMMON.name() : dto.getClassification())
                .build();

        folders.add(dto.getFolder());

        mongoTemplate.save(news);
    }

    @Override
    public void editNews(NewsEditDTO dto) {
        News news = mongoTemplate.findById(dto.getId(), News.class);

        if(news == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "News doesn't exist");
        }

        news.setClassification(dto.getClassification());
        news.setFolder(dto.getFolder());
        news.setTitle(dto.getTitle());
        news.setImageUrl(dto.getImageUrl());
        news.setContent(dto.getContent());

        folders.add(dto.getFolder());

        newsCache.put(news.getId(), news);
        mongoTemplate.save(news);
    }

    @Override
    public void deleteNews(String newsId) {
        News news = mongoTemplate.findById(newsId, News.class);

        if(news == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "News doesn't exist");
        }

        newsCache.invalidate(newsId);
        mongoTemplate.remove(news);
    }

    @Override
    public List<String> getAllClassifications() {
        return Arrays.stream(Classification.values()).map(Enum::name).toList();
    }

    @Override
    public List<String> getAllFolders() {
        if (folders.size() < 5) {
            folders.clear();
            List<News> news = mongoTemplate.findAll(News.class);

            news.forEach(newsEntry -> folders.add(newsEntry.getFolder()));
        }

        return folders.stream().toList();
    }

    @Override
    public News getNews(String newsId) {
        if(newsCache.getIfPresent(newsId) != null) {
            return newsCache.getIfPresent(newsId);
        }

        News news = mongoTemplate.findById(newsId, News.class);

        if(news == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "News doesn't exist");
        } else {
            newsCache.put(newsId, news);
            return news;
        }
    }

    @Override
    public List<News> getAllNews() {
        return mongoTemplate.findAll(News.class);
    }

    @Override
    public List<News> getNewsFilter(String classification, String folder, String keyword) {
        Query query = new Query();
        if(classification != null) {
            query.addCriteria(Criteria.where("classification").is(classification));
        }

        if(folder != null) {
            query.addCriteria(Criteria.where("folder").is(folder));
        }

        if(keyword != null) {
            query.addCriteria(Criteria.where("name").regex(".*" + keyword + ".*", "i"));
        }

        return mongoTemplate.find(query, News.class);
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void refreshFolders() {
        folders.clear();

        List<News> news = mongoTemplate.findAll(News.class);
        news.forEach(newsEntry -> folders.add(newsEntry.getFolder()));
    }
}
