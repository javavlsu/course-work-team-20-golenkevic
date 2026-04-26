package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.services.ProductService;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("newest", productService.getNewest(8).stream()
                .map(productService::convertToDto).toList());
        model.addAttribute("popular", productService.getPopular(8).stream()
                .map(productService::convertToDto).toList());
        model.addAttribute("categories", categoryRepository.findAll());
        return "index";
    }
}
