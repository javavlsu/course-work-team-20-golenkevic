package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vlsu.marketplace.dto.ProductDto;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.services.OrderService;
import ru.vlsu.marketplace.services.ProductService;
import ru.vlsu.marketplace.services.UserService;

import java.io.IOException;
import java.time.Instant;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/products")
    public String myProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("approved", productService.getBySellerAndStatus(user.getId(), Product.Status.APPROVED));
        model.addAttribute("pending", productService.getBySellerAndStatus(user.getId(), Product.Status.PENDING));
        model.addAttribute("rejected", productService.getBySellerAndStatus(user.getId(), Product.Status.REJECTED));
        return "seller/my_products";
    }

    @GetMapping("/products/new")
    public String addProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", Product.Condition.values());
        return "seller/add_product";
    }

    @PostMapping("/products/new")
    public String addProduct(@ModelAttribute ProductDto dto,
                             @RequestParam(required = false) MultipartFile image,
                             @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User seller = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCondition(dto.getCondition());
        product.setStatus(Product.Status.PENDING);
        product.setSeller(seller);
        product.setCreatedAt(Instant.now());
        if (dto.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        }
        if (image != null && !image.isEmpty()) {
            product.setImageData(image.getBytes());
        }
        productService.save(product);
        return "redirect:/seller/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id).orElseThrow();
        ProductDto dto = new ProductDto();
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCondition(product.getCondition());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        model.addAttribute("productDto", dto);
        model.addAttribute("productId", id);
        model.addAttribute("hasImage", product.getImageData() != null && product.getImageData().length > 0);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", Product.Condition.values());
        return "seller/edit_product";
    }

    @PostMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Integer id, @ModelAttribute ProductDto dto,
                              @RequestParam(required = false) MultipartFile image) throws IOException {
        Product product = productService.findById(id).orElseThrow();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCondition(dto.getCondition());
        if (dto.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(dto.getCategoryId()).orElse(null));
        }
        if (image != null && !image.isEmpty()) {
            product.setImageData(image.getBytes());
        }
        productService.save(product);
        return "redirect:/seller/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Integer id) {
        Product product = productService.findById(id).orElseThrow();
        product.setStatus(Product.Status.REMOVED);
        productService.save(product);
        return "redirect:/seller/products";
    }

    @GetMapping("/orders")
    public String sellerOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("orders", orderService.getOrdersBySeller(user.getId()));
        return "seller/seller_orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Integer id,
                                    @RequestParam ru.vlsu.marketplace.entities.Order.Status status) {
        orderService.updateStatus(id, status);
        return "redirect:/seller/orders";
    }
}
