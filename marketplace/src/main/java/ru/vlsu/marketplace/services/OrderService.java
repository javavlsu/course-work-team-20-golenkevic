package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.marketplace.dto.OrderDto;
import ru.vlsu.marketplace.entities.*;
import ru.vlsu.marketplace.repositories.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Transactional
    public Order createOrder(User buyer, OrderDto dto) {
        List<CartItem> cartItems = cartService.getCartItems(buyer.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Корзина пуста");
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(Order.Status.NEW);
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setCreatedAt(Instant.now());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());
            items.add(oi);
            total = total.add(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setItems(items);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartService.clearCart(buyer.getId());
        return saved;
    }

    public List<Order> getOrdersByBuyer(Integer buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    public List<Order> getOrdersBySeller(Integer sellerId) {
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order updateStatus(Integer orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public long count() {
        return orderRepository.count();
    }
}
