package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.vlsu.marketplace.dto.CatalogProductDto;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.repositories.ProductRepository;
import ru.vlsu.marketplace.repositories.ReviewRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    public void delete(Product product) {
        productRepository.delete(product);
    }

    public Page<Product> getApprovedProducts(Pageable pageable, Integer categoryId, Integer brandId,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              Product.Condition condition, Product.Gender gender, Product.Season season,
                                              String color, String material, String size, String search) {
        return productRepository.findWithFilters(categoryId, brandId, minPrice, maxPrice, condition,
                gender, season, color, material, size, search, pageable);
    }

    public List<String> getDistinctColors() { return productRepository.findDistinctColors(); }
    public List<String> getDistinctMaterials() { return productRepository.findDistinctMaterials(); }
    public List<String> getDistinctSizes() { return productRepository.findDistinctSizes(); }

    public List<Product> getNewest(int count) {
        return productRepository.findNewest(PageRequest.of(0, count));
    }

    public List<Product> getSimilar(Integer categoryId, Integer excludeId, int count) {
        if (categoryId == null) return List.of();
        return productRepository.findSimilar(categoryId, excludeId, PageRequest.of(0, count));
    }

    public List<Product> getPopular(int count) {
        return productRepository.findPopular(PageRequest.of(0, count));
    }

    public List<Product> getBySellerAndStatus(Integer sellerId, Product.Status status) {
        return productRepository.findBySellerIdAndStatus(sellerId, status);
    }

    public List<Product> getPendingProducts() {
        return productRepository.findByStatusOrderByCreatedAtDesc(Product.Status.PENDING);
    }

    public CatalogProductDto convertToDto(Product p) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(p.getId());
        long reviewsCount = reviewRepository.countByProductId(p.getId());

        return CatalogProductDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .condition(p.getCondition())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "")
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : "")
                .gender(p.getGender())
                .season(p.getSeason())
                .color(p.getColor())
                .material(p.getMaterial())
                .size(p.getSize())
                .sellerUsername(p.getSeller().getUsername())
                .averageRating(avgRating)
                .reviewsCount((int) reviewsCount)
                .hasImage(p.getImageData() != null && p.getImageData().length > 0)
                .themeColor(getConditionColor(p.getCondition()))
                .build();
    }

    private String getConditionColor(Product.Condition condition) {
        return switch (condition) {
            case NEW -> "from-emerald-500 to-teal-600";
            case USED -> "from-amber-500 to-orange-600";
            case VINTAGE -> "from-purple-500 to-indigo-600";
        };
    }

    public long countByStatus(Product.Status status) {
        return productRepository.countByStatus(status);
    }
}
