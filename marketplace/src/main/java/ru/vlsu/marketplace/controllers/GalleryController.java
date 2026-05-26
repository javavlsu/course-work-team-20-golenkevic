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
     * Поиск картинок во внешних API: DummyJSON + Platzi Fake Store.
     * Возвращает большую подборку реальных фото товаров. Не требует API-ключа.
     */
    @GetMapping("/external")
    public List<Map<String, String>> searchExternal(@RequestParam(required = false) String query,
                                                     @RequestParam(required = false) String category) {
        String q = (query != null && !query.isBlank()) ? query.trim() : CATEGORY_HINTS.getOrDefault(category, "");
        if (q.isBlank()) return List.of();

        List<Map<String, String>> result = new ArrayList<>();
        result.addAll(searchDummyJson(q));
        result.addAll(searchPlatzi(q));
        result.addAll(searchFakeStore(q));
        result.addAll(generatePicsum(q, 16));
        log.debug("External search: query={}, total={}", q, result.size());
        return result;
    }

    private List<Map<String, String>> searchDummyJson(String q) {
        try {
            String url = "https://dummyjson.com/products/search?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&limit=50";
            HttpResponse<String> resp = httpClient.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return List.of();
            JsonNode products = objectMapper.readTree(resp.body()).get("products");
            if (products == null || !products.isArray()) return List.of();

            List<Map<String, String>> list = new ArrayList<>();
            for (JsonNode p : products) {
                // Берём первое полноразмерное изображение, fallback на thumbnail
                String img = null;
                JsonNode imgs = p.get("images");
                if (imgs != null && imgs.isArray() && !imgs.isEmpty()) {
                    img = imgs.get(0).asText(null);
                }
                if (img == null || img.isBlank()) img = p.path("thumbnail").asText(null);
                if (img == null || img.isBlank()) continue;
                Map<String, String> item = new HashMap<>();
                item.put("url", img);
                item.put("title", p.path("title").asText(""));
                item.put("brand", p.path("brand").asText(""));
                item.put("source", "DummyJSON");
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            log.warn("DummyJSON failed: {}", e.getMessage());
            return List.of();
        }
    }

    /** Fake Store API — 20 реальных товаров, без поиска. Фильтруем локально. */
    private List<Map<String, String>> searchFakeStore(String q) {
        try {
            HttpResponse<String> resp = httpClient.send(
                    HttpRequest.newBuilder(URI.create("https://fakestoreapi.com/products"))
                            .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return List.of();
            JsonNode arr = objectMapper.readTree(resp.body());
            if (arr == null || !arr.isArray()) return List.of();

            String needle = q.toLowerCase();
            List<Map<String, String>> list = new ArrayList<>();
            for (JsonNode p : arr) {
                String title = p.path("title").asText("");
                String cat = p.path("category").asText("");
                if (!title.toLowerCase().contains(needle) && !cat.toLowerCase().contains(needle)) continue;
                String img = p.path("image").asText(null);
                if (img == null || img.isBlank()) continue;
                Map<String, String> item = new HashMap<>();
                item.put("url", img);
                item.put("title", title);
                item.put("source", "FakeStore");
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            log.warn("FakeStore failed: {}", e.getMessage());
            return List.of();
        }
    }

    /** Lorem Picsum — генерируем красивые рандомные фото с seed. Не требует сетевого вызова. */
    private List<Map<String, String>> generatePicsum(String q, int count) {
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String seed = q.replaceAll("[^a-zA-Zа-яА-Я0-9]", "") + "-" + i;
            Map<String, String> item = new HashMap<>();
            item.put("url", "https://picsum.photos/seed/" + URLEncoder.encode(seed, StandardCharsets.UTF_8) + "/600/800");
            item.put("title", q + " #" + i);
            item.put("source", "Picsum");
            list.add(item);
        }
        return list;
    }

    private List<Map<String, String>> searchPlatzi(String q) {
        try {
            String url = "https://api.escuelajs.co/api/v1/products/?title=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            HttpResponse<String> resp = httpClient.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return List.of();
            JsonNode arr = objectMapper.readTree(resp.body());
            if (arr == null || !arr.isArray()) return List.of();

            List<Map<String, String>> list = new ArrayList<>();
            for (JsonNode p : arr) {
                JsonNode imgs = p.get("images");
                if (imgs == null || !imgs.isArray() || imgs.isEmpty()) continue;
                String img = imgs.get(0).asText(null);
                // Platzi иногда возвращает строки с кавычками внутри JSON-массива
                if (img != null) img = img.replaceAll("^[\\[\"]+|[\\]\"]+$", "");
                if (img == null || img.isBlank() || !img.startsWith("http")) continue;
                Map<String, String> item = new HashMap<>();
                item.put("url", img);
                item.put("title", p.path("title").asText(""));
                item.put("source", "Platzi");
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            log.warn("Platzi failed: {}", e.getMessage());
            return List.of();
        }
    }
}
