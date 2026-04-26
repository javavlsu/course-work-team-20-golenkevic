package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vlsu.marketplace.dto.ProductDto;
import ru.vlsu.marketplace.dto.ReviewDto;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.Review;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.repositories.ProductImageRepository;
import ru.vlsu.marketplace.services.*;

import java.io.IOException;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final ProductImageRepository productImageRepository;

    @GetMapping("/product/{id}")
    public String productPage(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Product product = productService.findById(id).orElseThrow();
        model.addAttribute("product", productService.convertToDto(product));
        model.addAttribute("productEntity", product);
        model.addAttribute("reviews", reviewService.getByProduct(id));

        Integer categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        model.addAttribute("similar", productService.getSimilar(categoryId, id, 8)
                .stream().map(productService::convertToDto).toList());
        model.addAttribute("newest", productService.getNewest(8)
                .stream().filter(p -> !p.getId().equals(id))
                .map(productService::convertToDto).toList());

        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            model.addAttribute("isFavorite", favoriteService.isFavorite(user.getId(), id));
            model.addAttribute("hasReviewed", reviewService.reviewExists(user.getId(), id));
        }
        return "product";
    }

    @GetMapping("/product/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> productImage(@PathVariable Integer id) {
        Product product = productService.findById(id).orElseThrow();
        if (product.getImageData() != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(product.getImageData());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/product/{productId}/image/{imageId}")
    @ResponseBody
    public ResponseEntity<byte[]> productImageById(@PathVariable Integer productId, @PathVariable Integer imageId) {
        var image = productImageRepository.findById(imageId).orElseThrow();
        if (!image.getProduct().getId().equals(productId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image.getImageData());
    }

    @PostMapping("/product/{id}/review")
    public String addReview(@PathVariable Integer id, @ModelAttribute ReviewDto dto,
                            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Product product = productService.findById(id).orElseThrow();

        if (!reviewService.reviewExists(user.getId(), id)) {
            Review review = new Review();
            review.setProduct(product);
            review.setAuthor(user);
            review.setRating(dto.getRating());
            review.setText(dto.getText());
            review.setCreatedAt(Instant.now());
            reviewService.save(review);
        }
        return "redirect:/product/" + id;
    }
}
