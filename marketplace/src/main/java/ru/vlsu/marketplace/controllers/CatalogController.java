package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vlsu.marketplace.dto.CatalogProductDto;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.services.ProductService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) Integer categoryId,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          @RequestParam(required = false) Product.Condition condition,
                          @RequestParam(required = false) String search,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "createdAt") String sort,
                          Model model) {

        Pageable pageable = PageRequest.of(page, 20, Sort.by(sort).descending());
        Page<Product> products = productService.getApprovedProducts(pageable, categoryId, minPrice, maxPrice, condition, search);

        model.addAttribute("products", products.map(productService::convertToDto));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedCondition", condition);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("search", search);
        model.addAttribute("conditions", Product.Condition.values());
        return "catalog";
    }
}
