package ru.vlsu.marketplace.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.marketplace.entities.Brand;
import ru.vlsu.marketplace.entities.Category;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.BrandRepository;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.repositories.ProductRepository;
import ru.vlsu.marketplace.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        Category cat = new Category();
        cat.setName("Обувь");
        cat = categoryRepository.save(cat);

        Brand brand = new Brand();
        brand.setName("Nike");
        brand = brandRepository.save(brand);

        User seller = new User();
        seller.setUsername("seller1");
        seller.setEmail("seller1@test.ru");
        seller.setPasswordHash(passwordEncoder.encode("pwd"));
        seller.setRole(User.Role.seller);
        seller.setActive(true);
        seller.setRegisteredAt(Instant.now());
        seller = userRepository.save(seller);

        Product approved = new Product();
        approved.setTitle("Nike Air Max");
        approved.setDescription("Хорошие кроссовки");
        approved.setPrice(new BigDecimal("5000.00"));
        approved.setCondition(Product.Condition.NEW);
        approved.setStatus(Product.Status.APPROVED);
        approved.setCategory(cat);
        approved.setBrand(brand);
        approved.setSeller(seller);
        approved.setCreatedAt(Instant.now());
        productRepository.save(approved);

        Product pending = new Product();
        pending.setTitle("Спрятанный товар");
        pending.setPrice(new BigDecimal("100.00"));
        pending.setCondition(Product.Condition.USED);
        pending.setStatus(Product.Status.PENDING);
        pending.setSeller(seller);
        pending.setCreatedAt(Instant.now());
        productRepository.save(pending);
    }

    @Test
    @DisplayName("GET /catalog возвращает 200 и показывает APPROVED товары")
    void catalog_returnsApprovedProducts() throws Exception {
        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog"))
                .andExpect(content().string(containsString("Nike Air Max")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Спрятанный товар"))));
    }

    @Test
    @DisplayName("Фильтр по бренду работает")
    void catalog_brandFilter() throws Exception {
        Integer brandId = brandRepository.findByName("Nike").orElseThrow().getId();
        mockMvc.perform(get("/catalog").param("brandId", brandId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nike Air Max")));
    }

    @Test
    @DisplayName("Пустые строки в фильтрах не ломают поиск")
    void catalog_emptyStringFiltersAreIgnored() throws Exception {
        mockMvc.perform(get("/catalog")
                        .param("color", "")
                        .param("material", "")
                        .param("size", "")
                        .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nike Air Max")));
    }
}
