package ru.yandex.practicum.filmorate.modelTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldCreateValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений валидации");
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
    void shouldUseLoginWhenNameIsBlank() {
        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .login("testuser")
                .name("   ")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertEquals("testuser", user.getName(), "Должен использоваться логин, когда имя состоит из пробелов");
    }

    @Test
    void shouldUseLoginWhenNameIsNull() {
        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .login("testuser")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertEquals("testuser", user.getName(), "Должен использоваться логин, когда имя null");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        User user = User.builder()
                .id(1L)
                .email("")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с пустым email должен быть невалидным");
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        User user = User.builder()
                .id(1L)
                .email(null)
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с null email должен быть невалидным");
    }

    @Test
    void shouldFailWhenEmailHasNoAtSymbol() {
        User user = User.builder()
                .id(1L)
                .email("invalid-email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с email без @ должен быть невалидным");
    }

    @Test
    void shouldFailWhenEmailHasInvalidFormat() {
        User user = User.builder()
                .id(1L)
                .email("invalid@")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с невалидным форматом email должен быть невалидным");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с пустым логином должен быть невалидным");
    }

    @Test
    void shouldFailWhenLoginIsNull() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login(null)
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с null логином должен быть невалидным");
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("login with spaces")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим пробелы должен быть невалидным");
    }

    @Test
    void shouldFailWhenLoginIsTooShort() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("abc")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с логином короче 4 символов должен быть невалидным");
    }

    @Test
    void shouldAllowMinLengthLogin() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("abcd")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 4 символа должен быть валидным");
    }

    @Test
    void shouldFailWhenLoginIsTooLong() {
        String longLogin = "a".repeat(21);
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login(longLogin)
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с логином длиннее 20 символов должен быть невалидным");
    }

    @Test
    void shouldAllowMaxLengthLogin() {
        String maxLengthLogin = "a".repeat(20);
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login(maxLengthLogin)
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 20 символов должен быть валидным");
    }

    @Test
    void shouldFailWhenLoginContainsInvalidCharacters() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("invalid-login!")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим недопустимые символы должен быть невалидным");
    }

    @Test
    void shouldAllowValidCharactersInLogin() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("user_123")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с логином из латинских букв, цифр и подчеркивания должен быть валидным");
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с датой рождения в будущем должен быть невалидным");
    }

    @Test
    void shouldAllowCurrentDateAsBirthday() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.now())
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с сегодняшней датой рождения должен быть валидным");
    }

    @Test
    void shouldAllowPastBirthday() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.now().minusYears(1))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с датой рождения в прошлом должен быть валидным");
    }
}