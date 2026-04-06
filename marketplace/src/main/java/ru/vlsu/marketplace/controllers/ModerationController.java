package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.services.ProductService;

@Controller
@RequestMapping("/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ProductService productService;

    @GetMapping
    public String moderationPage(Model model) {
        model.addAttribute("pendingProducts", productService.getPendingProducts());
        return "moderation";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Integer id) {
        Product product = productService.findById(id).orElseThrow();
        product.setStatus(Product.Status.APPROVED);
        productService.save(product);
        return "redirect:/moderation";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Integer id) {
        Product product = productService.findById(id).orElseThrow();
        product.setStatus(Product.Status.REJECTED);
        productService.save(product);
        return "redirect:/moderation";
    }
}
