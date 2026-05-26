package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private static final Logger log = LoggerFactory.getLogger(GalleryController.class);
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

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
}
