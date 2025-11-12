package ru.yandex.practicum.filmorate.modelTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        validUser = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldCreateValidUserForCreation() {
        User userForCreate = User.builder()
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(userForCreate, CreateValidation.class);
        assertTrue(violations.isEmpty(), "Валидный пользователь для создания не должен иметь нарушений валидации");
    }

    @Test
    void shouldCreateValidUserForUpdate() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser, UpdateValidation.class);
        assertTrue(violations.isEmpty(), "Валидный пользователь для обновления не должен иметь нарушений валидации");
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .login("testuser")
                .name("")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertEquals("testuser", user.getName(), "Должен использоваться логин, когда имя пустое");
    }

    @Test
    void shouldFailWhenEmailIsBlankForCreation() {
        User user = User.builder()
                .email("")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с пустым email должен быть невалидным для создания");
    }

    @Test
    void shouldNotFailWhenEmailIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("") // Для обновления пустой email не проверяется @NotBlank
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        // Проверяем только что нет ошибок связанных с email из-за @NotBlank
        boolean hasEmailBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                        v.getMessage().contains("не может быть пустым"));
        assertFalse(hasEmailBlankViolation, "Для обновления пустой email не должен проверяться на @NotBlank");
    }

    @Test
    void shouldFailWhenEmailIsNullForCreation() {
        User user = User.builder()
                .email(null)
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с null email должен быть невалидным для создания");
    }

    @Test
    void shouldFailWhenEmailHasNoAtSymbolForCreation() {
        User user = User.builder()
                .email("invalid-email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с email без @ должен быть невалидным");
    }

    @Test
    void shouldFailWhenEmailHasNoAtSymbolForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("invalid-email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с email без @ должен быть невалидным для обновления");
    }

    @Test
    void shouldFailWhenLoginIsBlankForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с пустым логином должен быть невалидным для создания");
    }

    @Test
    void shouldNotFailWhenLoginIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("") // Для обновления пустой логин не проверяется @NotBlank
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        // Проверяем только что нет ошибок связанных с логином из-за @NotBlank
        boolean hasLoginBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login") &&
                        v.getMessage().contains("не может быть пустым"));
        assertFalse(hasLoginBlankViolation, "Для обновления пустой логин не должен проверяться на @NotBlank");
    }

    @Test
    void shouldFailWhenLoginIsNullForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login(null)
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с null логином должен быть невалидным для создания");
    }

    @Test
    void shouldFailWhenLoginContainsSpacesForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("login with spaces")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим пробелы должен быть невалидным");
    }

    @Test
    void shouldFailWhenLoginContainsSpacesForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("login with spaces")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим пробелы должен быть невалидным для обновления");
    }

    @Test
    void shouldFailWhenLoginIsTooShortForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abc")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с логином короче 4 символов должен быть невалидным");
    }

    @Test
    void shouldAllowMinLengthLoginForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abcd")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 4 символа должен быть валидным");
    }

    @Test
    void shouldFailWhenLoginIsTooLongForCreation() {
        String longLogin = "a".repeat(21);
        User user = User.builder()
                .email("valid@email.com")
                .login(longLogin)
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с логином длиннее 20 символов должен быть невалидным");
    }

    @Test
    void shouldFailWhenBirthdayIsInFutureForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с датой рождения в будущем должен быть невалидным");
    }

    @Test
    void shouldFailWhenBirthdayIsInFutureForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с датой рождения в будущем должен быть невалидным для обновления");
    }

    @Test
    void shouldFailWhenIdIsNullForUpdate() {
        User user = User.builder()
                .id(null) // Для обновления id обязателен
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        assertFalse(violations.isEmpty(), "Пользователь с null ID должен быть невалидным для обновления");
    }
}