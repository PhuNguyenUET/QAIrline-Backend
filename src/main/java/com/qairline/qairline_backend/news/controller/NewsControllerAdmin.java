package com.qairline.qairline_backend.news.controller;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.news.dto.NewsAddRequestDTO;
import com.qairline.qairline_backend.news.dto.NewsEditDTO;
import com.qairline.qairline_backend.news.model.News;
import com.qairline.qairline_backend.news.service.NewsService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "news_admin")
@RequestMapping("/api/admin/v1/news")
public class NewsControllerAdmin {
    @Value("${api.token}")
    private String apiToken;

    private final NewsService newsService;

    @GetMapping("/filter_news")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = News.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> filterNews(@RequestHeader("X-auth-token") String token,
                                                  @RequestParam(required = false) String classification,
                                                  @RequestParam(required = false) String folder,
                                                  @RequestParam(required = false) String keyword
    ) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<News> news = newsService.getNewsFilter(classification, folder, keyword);
            return ResponseEntity.ok(ApiResponse.success("Get filter news successful", news));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = News.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getNews(@RequestHeader("X-auth-token") String token,
                                               @RequestParam String newsId
    ) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            News news = newsService.getNews(newsId);
            return ResponseEntity.ok(ApiResponse.success("Get news successful", news));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_all")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = News.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllNews(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<News> news = newsService.getAllNews();
            return ResponseEntity.ok(ApiResponse.success("Get all news successful", news));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addNews (@RequestHeader("X-auth-token") String token,
                                                @RequestBody NewsAddRequestDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            newsService.addNews(dto);
            return ResponseEntity.ok(ApiResponse.success("Add news successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> editNews (@RequestHeader("X-auth-token") String token,
                                                 @RequestBody NewsEditDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            newsService.editNews(dto);
            return ResponseEntity.ok(ApiResponse.success("Edit news successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteNews(@RequestHeader("X-auth-token") String token,
                                                  @RequestParam String newsId
    ) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            newsService.deleteNews(newsId);
            return ResponseEntity.ok(ApiResponse.success("Delete news successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/classification")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getClassifications(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<String> classifications = newsService.getAllClassifications();
            return ResponseEntity.ok(ApiResponse.success("Get classifications successful", classifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/folders")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getFolders(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            List<String> folders = newsService.getAllFolders();
            return ResponseEntity.ok(ApiResponse.success("Get classifications successful", folders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
