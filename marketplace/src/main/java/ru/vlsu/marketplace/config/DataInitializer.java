package ru.vlsu.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Админ
        userRepository.findByUsername("admin").ifPresentOrElse(
            existing -> {
                existing.setPasswordHash(passwordEncoder.encode("admin123"));
                existing.setRole(User.Role.admin);
                existing.setActive(true);
                userRepository.save(existing);
            },
            () -> {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@marketplace.ru");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.admin);
                admin.setActive(true);
                admin.setRegisteredAt(Instant.now());
                userRepository.save(admin);
            }
        );

        // Бренды (создаём независимо от наличия товаров)
        getOrCreateBrand("Nike", "Американский бренд спортивной одежды и обуви");
        getOrCreateBrand("Adidas", "Немецкий производитель спортивной одежды");
        getOrCreateBrand("Apple", "Американская технологическая компания");
        getOrCreateBrand("Sony", "Японская корпорация электроники");
        getOrCreateBrand("Casio", "Японский производитель часов");
        getOrCreateBrand("Dr. Martens", "Британский бренд обуви");
        getOrCreateBrand("Balenciaga", "Французский люксовый дом моды");
        getOrCreateBrand("Chrome Hearts", "Американский бренд украшений и одежды");
        getOrCreateBrand("Без бренда", "Товары без указания бренда");

        if (productRepository.count() > 0) return;

        // Категории
        Category clothing  = getOrCreateCategory("Одежда");
        Category shoes     = getOrCreateCategory("Обувь");
        Category bags      = getOrCreateCategory("Сумки");
        Category accessories = getOrCreateCategory("Аксессуары");
        Category electronics = getOrCreateCategory("Электроника");

        // Бренды для использования
        Brand nike = brandRepository.findByName("Nike").orElseThrow();
        Brand apple = brandRepository.findByName("Apple").orElseThrow();
        Brand sony = brandRepository.findByName("Sony").orElseThrow();
        Brand casio = brandRepository.findByName("Casio").orElseThrow();
        Brand drMartens = brandRepository.findByName("Dr. Martens").orElseThrow();
        Brand chromeHearts = brandRepository.findByName("Chrome Hearts").orElseThrow();
        Brand noBrand = brandRepository.findByName("Без бренда").orElseThrow();

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

        // Тестовые товары: title, desc, price, condition, category, brand, gender, season, color, material, size
        List<Object[]> items = List.of(
            row("Кожаная куртка оверсайз", "Классическая куртка из натуральной кожи. Состояние отличное, носилась 1 сезон.", "8500.00", Product.Condition.USED, clothing, noBrand, Product.Gender.UNISEX, Product.Season.DEMI, "Чёрный", "Натуральная кожа", "L"),
            row("Белая рубашка Oxford", "Оксфордская рубашка из 100% хлопка, размер M. Новая, без бирок.", "2200.00", Product.Condition.NEW, clothing, noBrand, Product.Gender.MALE, Product.Season.UNIVERSAL, "Белый", "Хлопок", "M"),
            row("Джинсы straight fit", "Классические прямые джинсы тёмно-синего цвета.", "3400.00", Product.Condition.USED, clothing, noBrand, Product.Gender.MALE, Product.Season.UNIVERSAL, "Тёмно-синий", "Деним", "32"),
            row("Кроссовки Nike Air Max", "Nike Air Max 90, размер 42. Состояние хорошее, есть небольшие потёртости.", "5900.00", Product.Condition.USED, shoes, nike, Product.Gender.MALE, Product.Season.DEMI, "Белый", "Текстиль", "42"),
            row("Кроссовки Nike Dunk", "Nike Dunk Low Retro, размер 43. Новые в коробке.", "9500.00", Product.Condition.NEW, shoes, nike, Product.Gender.UNISEX, Product.Season.DEMI, "Чёрный", "Кожа", "43"),
            row("Ботинки Dr. Martens 1460", "Классические ботинки Dr. Martens, размер 41. Новые в коробке.", "12000.00", Product.Condition.NEW, shoes, drMartens, Product.Gender.UNISEX, Product.Season.WINTER, "Чёрный", "Натуральная кожа", "41"),
            row("Лоферы кожаные", "Элегантные кожаные лоферы, чёрные, размер 40.", "4200.00", Product.Condition.USED, shoes, noBrand, Product.Gender.FEMALE, Product.Season.SUMMER, "Чёрный", "Кожа", "40"),
            row("Тоут-сумка из холста", "Вместительная холщовая сумка с кожаными ручками. Новая.", "1800.00", Product.Condition.NEW, bags, noBrand, Product.Gender.UNISEX, Product.Season.UNIVERSAL, "Бежевый", "Холст", "F"),
            row("Кожаный рюкзак vintage", "Рюкзак из натуральной кожи в винтажном стиле. Объём 25л.", "6700.00", Product.Condition.VINTAGE, bags, noBrand, Product.Gender.UNISEX, Product.Season.UNIVERSAL, "Коричневый", "Натуральная кожа", "F"),
            row("Серебряное кольцо Chrome Hearts", "Серебряное кольцо с гравировкой. Серебро 925. Размер 19.", "18500.00", Product.Condition.USED, accessories, chromeHearts, Product.Gender.UNISEX, Product.Season.UNIVERSAL, "Серебристый", "Серебро 925", "19"),
            row("Серебряный браслет Chrome Hearts", "Массивный браслет цепочка. Серебро 925.", "24000.00", Product.Condition.NEW, accessories, chromeHearts, Product.Gender.UNISEX, Product.Season.UNIVERSAL, "Серебристый", "Серебро 925", "F"),
            row("Золотые серьги-кольца", "Серьги-кольца из позолоченного серебра 925. Диаметр 4 см.", "950.00", Product.Condition.NEW, accessories, noBrand, Product.Gender.FEMALE, Product.Season.UNIVERSAL, "Золотой", "Позолота", "F"),
            row("Механические часы Casio", "Casio MTP-V001, нержавеющая сталь. Работают отлично.", "3100.00", Product.Condition.USED, accessories, casio, Product.Gender.MALE, Product.Season.UNIVERSAL, "Серебристый", "Сталь", "F"),
            row("iPhone 12 64GB", "iPhone 12 чёрный, 64GB. Аккумулятор 87%, комплект полный.", "28000.00", Product.Condition.USED, electronics, apple, Product.Gender.UNISEX, Product.Season.UNIVERSAL, "Чёрный", "Алюминий", "F"),
            row("Sony WH-1000XM4", "Беспроводные наушники с ANC. Состояние отличное.", "14500.00", Product.Condition.USED, electronics, sony, Product.Gender.UNISEX, Product.Season.UNIVERSAL, "Чёрный", "Пластик", "F")
        );

        for (Object[] item : items) {
            Product p = new Product();
            p.setTitle((String) item[0]);
            p.setDescription((String) item[1]);
            p.setPrice(new BigDecimal((String) item[2]));
            p.setCondition((Product.Condition) item[3]);
            p.setCategory((Category) item[4]);
            p.setBrand((Brand) item[5]);
            p.setGender((Product.Gender) item[6]);
            p.setSeason((Product.Season) item[7]);
            p.setColor((String) item[8]);
            p.setMaterial((String) item[9]);
            p.setSize((String) item[10]);
            p.setSeller(seller);
            p.setStatus(Product.Status.APPROVED);
            p.setCreatedAt(Instant.now());
            productRepository.save(p);
        }
    }

    private Object[] row(Object... values) { return values; }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category c = new Category();
            c.setName(name);
            return categoryRepository.save(c);
        });
    }

    private Brand getOrCreateBrand(String name, String description) {
        return brandRepository.findByName(name).orElseGet(() -> {
            Brand b = new Brand();
            b.setName(name);
            b.setDescription(description);
            return brandRepository.save(b);
        });
    }
}
