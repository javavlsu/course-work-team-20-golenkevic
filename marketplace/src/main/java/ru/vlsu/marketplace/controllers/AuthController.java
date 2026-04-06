package ru.vlsu.marketplace.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vlsu.marketplace.dto.UserRegistrationDto;
import ru.vlsu.marketplace.services.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/auth")
    public String authPage(Model model) {
        model.addAttribute("registrationDto", new UserRegistrationDto());
        return "auth";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute UserRegistrationDto dto, RedirectAttributes ra) {
        if (!dto.isPasswordConfirmed()) {
            ra.addFlashAttribute("error", "Пароли не совпадают");
            return "redirect:/auth?error";
        }
        if (userService.isUsernameTaken(dto.getUsername())) {
            ra.addFlashAttribute("error", "Имя пользователя занято");
            return "redirect:/auth?error";
        }
        if (userService.isEmailTaken(dto.getEmail())) {
            ra.addFlashAttribute("error", "Email уже используется");
            return "redirect:/auth?error";
        }
        userService.registerNewUser(dto);
        ra.addFlashAttribute("success", "Регистрация успешна! Войдите в систему.");
        return "redirect:/auth?success";
    }
}
