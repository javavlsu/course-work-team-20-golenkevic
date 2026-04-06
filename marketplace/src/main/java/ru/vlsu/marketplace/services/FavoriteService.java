package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.marketplace.entities.Favorite;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.FavoriteRepository;
import ru.vlsu.marketplace.repositories.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    public void addToFavorites(User user, Integer productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            Product product = productRepository.findById(productId).orElseThrow();
            Favorite fav = new Favorite();
            fav.setUser(user);
            fav.setProduct(product);
            favoriteRepository.save(fav);
        }
    }

    public void removeFromFavorites(Integer userId, Integer productId) {
        favoriteRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(favoriteRepository::delete);
    }

    public List<Favorite> getFavorites(Integer userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public boolean isFavorite(Integer userId, Integer productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
