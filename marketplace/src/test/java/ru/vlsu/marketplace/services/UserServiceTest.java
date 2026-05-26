package ru.vlsu.marketplace.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.vlsu.marketplace.dto.UserRegistrationDto;
import ru.vlsu.marketplace.entities.User;
import ru.vlsu.marketplace.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("Регистрация хеширует пароль и ставит роль buyer")
    void registerNewUser_hashesPasswordAndSetsBuyerRole() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setPassword("secret");

        when(passwordEncoder.encode("secret")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerNewUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();

        assertThat(captured.getUsername()).isEqualTo("alice");
        assertThat(captured.getEmail()).isEqualTo("alice@example.com");
        assertThat(captured.getPasswordHash()).isEqualTo("ENCODED");
        assertThat(captured.getRole()).isEqualTo(User.Role.buyer);
        assertThat(captured.isActive()).isTrue();
        assertThat(captured.getRegisteredAt()).isNotNull();
        assertThat(saved).isNotNull();
    }

    @Test
    @DisplayName("isUsernameTaken возвращает true если имя занято")
    void isUsernameTaken_returnsTrue() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        assertThat(userService.isUsernameTaken("alice")).isTrue();
    }

    @Test
    @DisplayName("isEmailTaken возвращает false если email свободен")
    void isEmailTaken_returnsFalse() {
        when(userRepository.existsByEmail("free@example.com")).thenReturn(false);
        assertThat(userService.isEmailTaken("free@example.com")).isFalse();
    }
}
