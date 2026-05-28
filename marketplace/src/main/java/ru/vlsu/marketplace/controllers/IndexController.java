package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.marketplace.repositories.BrandRepository;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.repositories.ProductRepository;
import ru.vlsu.marketplace.repositories.UserRepository;
import ru.vlsu.marketplace.services.ProductService;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("newest", productService.getNewest(8).stream()
                .map(productService::convertToDto).toList());
        model.addAttribute("popular", productService.getPopular(8).stream()
                .map(productService::convertToDto).toList());
        model.addAttribute("categories", categoryRepository.findAll());
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("productsCount", productRepository.count());
        model.addAttribute("brandsCount", brandRepository.count());
        model.addAttribute("categoriesCount", categoryRepository.count());
        model.addAttribute("usersCount", userRepository.count());
        return "about";
    }
}
