package ru.vlsu.marketplace.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.marketplace.dto.ChangeRoleRequest;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.services.OrderService;
import ru.vlsu.marketplace.services.ProductService;
import ru.vlsu.marketplace.services.UserService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/users")
    public String userList(@RequestParam(required = false) String search,
                           @RequestParam(required = false) User.Role role,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<User> users = userService.findWithFilters(search, role, PageRequest.of(page, 20));
        model.addAttribute("users", users);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        return "admin/user_list";
    }

    @PostMapping("/users/change-role")
    @ResponseBody
    public ResponseEntity<String> changeRole(@RequestBody ChangeRoleRequest request) {
        User user = userService.findById(request.getUserId()).orElseThrow();
        user.setRole(request.getNewRole());
        userService.save(user);
        return ResponseEntity.ok("Роль изменена");
    }

    @PostMapping("/users/{id}/block")
    @ResponseBody
    public ResponseEntity<String> blockUser(@PathVariable Integer id) {
        User user = userService.findById(id).orElseThrow();
        user.setActive(false);
        userService.save(user);
        return ResponseEntity.ok("Пользователь заблокирован");
    }

    @PostMapping("/users/{id}/unblock")
    @ResponseBody
    public ResponseEntity<String> unblockUser(@PathVariable Integer id) {
        User user = userService.findById(id).orElseThrow();
        user.setActive(true);
        userService.save(user);
        return ResponseEntity.ok("Пользователь разблокирован");
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalProducts", productService.countByStatus(Product.Status.APPROVED));
        model.addAttribute("totalOrders", orderService.count());
        model.addAttribute("pendingProducts", productService.countByStatus(Product.Status.PENDING));
        return "admin/dashboard";
    }
}
