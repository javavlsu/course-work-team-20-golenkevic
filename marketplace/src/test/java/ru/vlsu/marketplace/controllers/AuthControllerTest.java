package ru.vlsu.marketplace.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.vlsu.marketplace.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /auth доступна без авторизации")
    void authPage_accessible() throws Exception {
        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    @DisplayName("Регистрация создаёт нового пользователя")
    void register_createsUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "newuser@test.ru")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth?success"));

        assertThat(userRepository.findByUsername("newuser")).isPresent();
    }

    @Test
    @DisplayName("Регистрация с разными паролями редиректит на ?register&error")
    void register_passwordMismatch_redirectsToError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "user2")
                        .param("email", "user2@test.ru")
                        .param("password", "secret123")
                        .param("confirmPassword", "wrong")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth?register&error"));

        assertThat(userRepository.findByUsername("user2")).isEmpty();
    }
}
