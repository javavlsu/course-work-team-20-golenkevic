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
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.repositories.BrandRepository;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.services.ProductService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) Integer categoryId,
                          @RequestParam(required = false) Integer brandId,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          @RequestParam(required = false) Product.Condition condition,
                          @RequestParam(required = false) Product.Gender gender,
                          @RequestParam(required = false) Product.Season season,
                          @RequestParam(required = false) String color,
                          @RequestParam(required = false) String material,
                          @RequestParam(required = false) String size,
                          @RequestParam(required = false) String search,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "createdAt") String sort,
                          Model model) {

        // Пустые строки превращаем в null, чтобы фильтры пропускали значения
        color = blankToNull(color);
        material = blankToNull(material);
        size = blankToNull(size);
        search = blankToNull(search);

        Pageable pageable = PageRequest.of(page, 20, Sort.by(sort).descending());
        Page<Product> products = productService.getApprovedProducts(pageable, categoryId, brandId, minPrice, maxPrice,
                condition, gender, season, color, material, size, search);

        model.addAttribute("products", products.map(productService::convertToDto));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedBrand", brandId);
        model.addAttribute("selectedCondition", condition);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedSeason", season);
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedMaterial", material);
        model.addAttribute("selectedSize", size);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("search", search);
        model.addAttribute("conditions", Product.Condition.values());
        model.addAttribute("genders", Product.Gender.values());
        model.addAttribute("seasons", Product.Season.values());
        model.addAttribute("availableColors", productService.getDistinctColors());
        model.addAttribute("availableMaterials", productService.getDistinctMaterials());
        model.addAttribute("availableSizes", productService.getDistinctSizes());
        return "catalog";
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
