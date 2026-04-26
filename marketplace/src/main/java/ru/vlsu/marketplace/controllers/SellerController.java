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
import ru.vlsu.marketplace.entities.ProductImage;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.BrandRepository;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.repositories.ProductImageRepository;
import ru.vlsu.marketplace.services.OrderService;
import ru.vlsu.marketplace.services.ProductService;
import ru.vlsu.marketplace.services.UserService;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.time.Instant;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductImageRepository productImageRepository;

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
        addFormReferenceData(model);
        return "seller/add_product";
    }

    @PostMapping("/products/new")
    public String addProduct(@ModelAttribute ProductDto dto,
                             @RequestParam(required = false) MultipartFile[] images,
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
        applyDtoAttributes(product, dto);
        Product saved = productService.save(product);
        saveImages(saved, images, 0);
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
        dto.setBrandId(product.getBrand() != null ? product.getBrand().getId() : null);
        dto.setGender(product.getGender());
        dto.setSeason(product.getSeason());
        dto.setColor(product.getColor());
        dto.setMaterial(product.getMaterial());
        dto.setSize(product.getSize());
        model.addAttribute("productDto", dto);
        model.addAttribute("productId", id);
        model.addAttribute("hasImage", product.getImageData() != null && product.getImageData().length > 0);
        model.addAttribute("extraImages", productImageRepository.findByProductIdOrderBySortOrderAsc(id));
        addFormReferenceData(model);
        return "seller/edit_product";
    }

    @PostMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Integer id, @ModelAttribute ProductDto dto,
                              @RequestParam(required = false) MultipartFile[] images) throws IOException {
        Product product = productService.findById(id).orElseThrow();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCondition(dto.getCondition());
        applyDtoAttributes(product, dto);
        Product saved = productService.save(product);
        int existingCount = productImageRepository.findByProductIdOrderBySortOrderAsc(id).size()
                          + (saved.getImageData() != null ? 1 : 0);
        saveImages(saved, images, existingCount);
        return "redirect:/seller/products";
    }

    private void addFormReferenceData(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("conditions", Product.Condition.values());
        model.addAttribute("genders", Product.Gender.values());
        model.addAttribute("seasons", Product.Season.values());
    }

    private void applyDtoAttributes(Product product, ProductDto dto) {
        product.setCategory(dto.getCategoryId() != null ? categoryRepository.findById(dto.getCategoryId()).orElse(null) : null);
        product.setBrand(dto.getBrandId() != null ? brandRepository.findById(dto.getBrandId()).orElse(null) : null);
        product.setGender(dto.getGender());
        product.setSeason(dto.getSeason());
        product.setColor(dto.getColor());
        product.setMaterial(dto.getMaterial());
        product.setSize(dto.getSize());
    }

    @PostMapping("/products/{productId}/images/{imageId}/delete")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> deleteImage(@PathVariable Integer productId, @PathVariable Integer imageId) {
        productImageRepository.findById(imageId).ifPresent(img -> {
            if (img.getProduct().getId().equals(productId)) {
                productImageRepository.delete(img);
            }
        });
        return org.springframework.http.ResponseEntity.ok("OK");
    }

    private void saveImages(Product product, MultipartFile[] images, int startSortOrder) throws IOException {
        if (images == null) return;
        List<ProductImage> toSave = new ArrayList<>();
        int order = startSortOrder;
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) continue;
            // Первое фото — в imageData (для совместимости с каталогом)
            if (product.getImageData() == null) {
                product.setImageData(file.getBytes());
                productService.save(product);
            } else {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageData(file.getBytes());
                img.setSortOrder(order++);
                toSave.add(img);
            }
        }
        if (!toSave.isEmpty()) {
            productImageRepository.saveAll(toSave);
        }
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
