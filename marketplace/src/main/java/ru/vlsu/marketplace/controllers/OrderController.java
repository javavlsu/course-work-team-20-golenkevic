package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.marketplace.dto.OrderDto;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.services.CartService;
import ru.vlsu.marketplace.services.OrderService;
import ru.vlsu.marketplace.services.UserService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/orders/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("cartItems", cartService.getCartItems(user.getId()));
        model.addAttribute("total", cartService.getCartTotal(user.getId()));
        model.addAttribute("orderDto", new OrderDto());
        return "checkout";
    }

    @PostMapping("/orders/checkout")
    public String checkout(@ModelAttribute OrderDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        orderService.createOrder(user, dto);
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("orders", orderService.getOrdersByBuyer(user.getId()));
        return "orders";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        var order = orderService.findById(id).orElseThrow();
        if (!order.getBuyer().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/orders";
        }
        if (order.getStatus() == ru.vlsu.marketplace.entities.Order.Status.NEW) {
            orderService.updateStatus(id, ru.vlsu.marketplace.entities.Order.Status.CANCELLED);
        }
        return "redirect:/orders";
    }
}
