package ru.vlsu.marketplace.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vlsu.marketplace.dto.CatalogProductDto;
import ru.vlsu.marketplace.entities.Brand;
import ru.vlsu.marketplace.entities.Category;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.ProductRepository;
import ru.vlsu.marketplace.repositories.ReviewRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ReviewRepository reviewRepository;
    @InjectMocks private ProductService productService;

    private Product sample;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("Одежда");

        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Nike");

        User seller = new User();
        seller.setId(1);
        seller.setUsername("demo_seller");

        sample = new Product();
        sample.setId(10);
        sample.setTitle("Кроссовки Nike");
        sample.setDescription("Описание");
        sample.setPrice(new BigDecimal("5000.00"));
        sample.setCondition(Product.Condition.NEW);
        sample.setStatus(Product.Status.APPROVED);
        sample.setCategory(cat);
        sample.setBrand(brand);
        sample.setSeller(seller);
        sample.setColor("Чёрный");
        sample.setSize("42");
    }

    @Test
    @DisplayName("convertToDto должен заполнить все основные поля")
    void convertToDto_populatesAllFields() {
        when(reviewRepository.getAverageRatingByProductId(10)).thenReturn(4.5);
        when(reviewRepository.countByProductId(10)).thenReturn(3L);

        CatalogProductDto dto = productService.convertToDto(sample);

        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getTitle()).isEqualTo("Кроссовки Nike");
        assertThat(dto.getPrice()).isEqualByComparingTo("5000.00");
        assertThat(dto.getCategoryName()).isEqualTo("Одежда");
        assertThat(dto.getBrandName()).isEqualTo("Nike");
        assertThat(dto.getColor()).isEqualTo("Чёрный");
        assertThat(dto.getSize()).isEqualTo("42");
        assertThat(dto.getAverageRating()).isEqualTo(4.5);
        assertThat(dto.getReviewsCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("convertToDto без категории не падает")
    void convertToDto_noCategory_doesNotFail() {
        sample.setCategory(null);
        when(reviewRepository.getAverageRatingByProductId(anyInt())).thenReturn(null);
        when(reviewRepository.countByProductId(anyInt())).thenReturn(0L);

        CatalogProductDto dto = productService.convertToDto(sample);

        assertThat(dto.getCategoryName()).isEmpty();
        assertThat(dto.getCategoryId()).isNull();
    }

    @Test
    @DisplayName("findById возвращает товар если он существует")
    void findById_returnsProduct() {
        when(productRepository.findById(10)).thenReturn(Optional.of(sample));

        Optional<Product> result = productService.findById(10);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Кроссовки Nike");
    }

    @Test
    @DisplayName("getSimilar возвращает пустой список если categoryId null")
    void getSimilar_nullCategory_returnsEmpty() {
        List<Product> result = productService.getSimilar(null, 1, 4);

        assertThat(result).isEmpty();
        verify(productRepository, never()).findSimilar(any(), any(), any());
    }

    @Test
    @DisplayName("save делегирует вызов в репозиторий")
    void save_delegatesToRepository() {
        when(productRepository.save(sample)).thenReturn(sample);

        Product saved = productService.save(sample);

        assertThat(saved).isSameAs(sample);
        verify(productRepository).save(sample);
    }
}
