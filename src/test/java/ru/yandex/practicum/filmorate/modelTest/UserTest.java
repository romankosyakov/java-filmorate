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
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldCreateValidUserForUpdate() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser, UpdateValidation.class);
        assertTrue(violations.isEmpty());
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

        assertEquals("testuser", user.getName());
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
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotFailWhenEmailIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        boolean hasEmailBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                        v.getMessage().contains("не может быть пустым"));
        assertFalse(hasEmailBlankViolation);
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotFailWhenLoginIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        boolean hasLoginBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login") &&
                        v.getMessage().contains("не может быть пустым"));
        assertFalse(hasLoginBlankViolation);
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertTrue(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
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
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenIdIsNullForUpdate() {
        User user = User.builder()
                .id(null)
                .email("valid@email.com")
                .login("valid_login")
                .name("Valid Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldInitializeUserFriendsAsEmptySet() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertNotNull(user.getUserFriends());
        assertTrue(user.getUserFriends().isEmpty());
    }
}