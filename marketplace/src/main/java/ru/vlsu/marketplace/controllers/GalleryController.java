package ru.vlsu.marketplace.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private static final Logger log = LoggerFactory.getLogger(GalleryController.class);
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Маппинг наших категорий в поисковые подсказки для DummyJSON
    private static final Map<String, String> CATEGORY_HINTS = Map.of(
            "clothes", "shirt",
            "shoes", "shoes",
            "bags", "bag",
            "accessories", "watch",
            "electronics", "phone"
    );

    @GetMapping
    public List<Map<String, String>> list(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) String search) throws Exception {
        Resource[] resources = resolver.getResources("classpath:/static/gallery/**/*.*");
        List<Map<String, String>> result = new ArrayList<>();

        for (Resource res : resources) {
            String filename = res.getFilename();
            if (filename == null) continue;
            String url = res.getURL().toString();
            int galleryIdx = url.indexOf("/gallery/");
            if (galleryIdx < 0) continue;
            String relative = url.substring(galleryIdx);
            // /gallery/clothes/odezda-1.jpg -> категория = clothes
            String[] parts = relative.split("/");
            if (parts.length < 4) continue;
            String cat = parts[2];

            if (category != null && !category.isBlank() && !cat.equalsIgnoreCase(category)) continue;
            String haystack = (filename + " " + cat).toLowerCase();
            if (search != null && !search.isBlank() && !haystack.contains(search.toLowerCase())) continue;

            result.add(Map.of(
                "path", relative,
                "filename", filename,
                "category", cat
            ));
        }
        log.debug("Gallery list: category={}, search={}, returned={}", category, search, result.size());
        return result;
    }

    /**
     * Поиск картинок во внешнем API (DummyJSON). Возвращает реальные фото товаров с названиями и ценами.
     * Не требует API-ключа.
     */
    @GetMapping("/external")
    public List<Map<String, String>> searchExternal(@RequestParam(required = false) String query,
                                                     @RequestParam(required = false) String category) {
        String q = (query != null && !query.isBlank()) ? query.trim() : CATEGORY_HINTS.getOrDefault(category, "");
        if (q.isBlank()) return List.of();
        try {
            String url = "https://dummyjson.com/products/search?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&limit=24";
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("External API returned status {}", resp.statusCode());
                return List.of();
            }
            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode products = root.get("products");
            if (products == null || !products.isArray()) return List.of();

            List<Map<String, String>> result = new ArrayList<>();
            for (JsonNode p : products) {
                String thumb = p.path("thumbnail").asText(null);
                if (thumb == null || thumb.isBlank()) continue;
                Map<String, String> item = new HashMap<>();
                item.put("url", thumb);
                item.put("title", p.path("title").asText(""));
                item.put("price", p.path("price").asText(""));
                item.put("brand", p.path("brand").asText(""));
                result.add(item);
            }
            log.debug("External API search: query={}, found={}", q, result.size());
            return result;
        } catch (Exception e) {
            log.warn("External API request failed: {}", e.getMessage());
            return List.of();
        }
    }
}
