package ru.vlsu.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.vlsu.marketplace.entities.Category;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.CategoryRepository;
import ru.vlsu.marketplace.repositories.ProductRepository;
import ru.vlsu.marketplace.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) return;

        // Категории
        Category clothing  = getOrCreateCategory("Одежда");
        Category shoes     = getOrCreateCategory("Обувь");
        Category bags      = getOrCreateCategory("Сумки");
        Category accessories = getOrCreateCategory("Аксессуары");
        Category electronics = getOrCreateCategory("Электроника");

        // Продавец-демо
        User seller = userRepository.findByUsername("demo_seller").orElseGet(() -> {
            User u = new User();
            u.setUsername("demo_seller");
            u.setEmail("demo_seller@marketplace.ru");
            u.setPasswordHash(passwordEncoder.encode("password123"));
            u.setRole(User.Role.seller);
            u.setActive(true);
            u.setRegisteredAt(Instant.now());
            return userRepository.save(u);
        });

        // Тестовые товары
        List<Object[]> items = List.of(
            new Object[]{"Кожаная куртка оверсайз", "Классическая куртка из натуральной кожи. Состояние отличное, носилась 1 сезон.", new BigDecimal("8500.00"),  Product.Condition.USED,    clothing},
            new Object[]{"Белая рубашка Oxford",     "Оксфордская рубашка из 100% хлопка, размер M. Новая, без бирок.",                new BigDecimal("2200.00"),  Product.Condition.NEW,     clothing},
            new Object[]{"Джинсы straight fit",      "Классические прямые джинсы тёмно-синего цвета. Размер 32/32.",                  new BigDecimal("3400.00"),  Product.Condition.USED,    clothing},
            new Object[]{"Кроссовки Nike Air Max",   "Nike Air Max 90, размер 42. Состояние хорошее, есть небольшие потёртости.",      new BigDecimal("5900.00"),  Product.Condition.USED,    shoes},
            new Object[]{"Ботинки Dr. Martens 1460", "Классические ботинки Dr. Martens, размер 41. Новые в коробке.",                 new BigDecimal("12000.00"), Product.Condition.NEW,     shoes},
            new Object[]{"Лоферы кожаные",           "Элегантные кожаные лоферы, чёрные, размер 40. Носились пару раз.",              new BigDecimal("4200.00"),  Product.Condition.USED,    shoes},
            new Object[]{"Тоут-сумка из холста",     "Вместительная холщовая сумка с кожаными ручками. Новая.",                       new BigDecimal("1800.00"),  Product.Condition.NEW,     bags},
            new Object[]{"Кожаный рюкзак vintage",   "Рюкзак из натуральной кожи в винтажном стиле. Объём 25л.",                     new BigDecimal("6700.00"),  Product.Condition.VINTAGE, bags},
            new Object[]{"Золотые серьги-кольца",    "Серьги-кольца из позолоченного серебра 925. Диаметр 4 см. Новые.",             new BigDecimal("950.00"),   Product.Condition.NEW,     accessories},
            new Object[]{"Механические часы Casio",  "Casio MTP-V001, нержавеющая сталь. Работают отлично, есть царапины на стекле.", new BigDecimal("3100.00"),  Product.Condition.USED,    accessories},
            new Object[]{"iPhone 12 64GB",           "iPhone 12 чёрный, 64GB. Аккумулятор 87%, комплект полный.",                    new BigDecimal("28000.00"), Product.Condition.USED,    electronics},
            new Object[]{"Sony WH-1000XM4",          "Беспроводные наушники с ANC. Состояние отличное, все документы.",              new BigDecimal("14500.00"), Product.Condition.USED,    electronics}
        );

        for (Object[] item : items) {
            Product p = new Product();
            p.setTitle((String) item[0]);
            p.setDescription((String) item[1]);
            p.setPrice((BigDecimal) item[2]);
            p.setCondition((Product.Condition) item[3]);
            p.setCategory((Category) item[4]);
            p.setSeller(seller);
            p.setStatus(Product.Status.APPROVED);
            p.setCreatedAt(Instant.now());
            productRepository.save(p);
        }
    }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category c = new Category();
            c.setName(name);
            return categoryRepository.save(c);
        });
    }
}
