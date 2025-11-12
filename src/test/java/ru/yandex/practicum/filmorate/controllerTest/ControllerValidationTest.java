package ru.yandex.practicum.filmorate.controllerTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ControllerValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // Тесты для Film - создание

    @Test
    void shouldFailValidationWhenFilmNameIsBlankForCreation() {
        Film film = Film.builder()
                .name("") // пустое название - должно не пройти валидацию
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с пустым названием должен быть невалидным для создания");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailValidationWhenFilmNameIsNullForCreation() {
        Film film = Film.builder()
                .name(null) // null название - должно не пройти валидацию
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с null названием должен быть невалидным для создания");
    }

    @Test
    void shouldFailValidationWhenFilmDescriptionIsTooLongForCreation() {
        String longDescription = "A".repeat(201); // 201 символ - больше лимита
        Film film = Film.builder()
                .name("Valid Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с описанием длиннее 200 символов должен быть невалидным для создания");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void shouldPassValidationWhenFilmDescriptionIsExactly200CharactersForCreation() {
        String maxLengthDescription = "A".repeat(200); // граничное значение
        Film film = Film.builder()
                .name("Valid Film")
                .description(maxLengthDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Фильм с описанием длиной 200 символов должен быть валидным для создания");
    }

    @Test
    void shouldFailValidationWhenFilmReleaseDateIsBeforeMinDateForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27)) // день до минимальной даты
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с датой релиза раньше 28.12.1895 должен быть невалидным для создания");
    }

    @Test
    void shouldPassValidationWhenFilmReleaseDateIsExactlyMinDateForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28)) // граничное значение
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Фильм с датой релиза 28.12.1895 должен быть валидным для создания");
    }

    @Test
    void shouldFailValidationWhenFilmDurationIsZeroForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0) // граничное значение - должно не пройти
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с продолжительностью 0 должен быть невалидным для создания");
    }

    @Test
    void shouldFailValidationWhenFilmDurationIsNegativeForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1) // отрицательное значение
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с отрицательной продолжительностью должен быть невалидным для создания");
    }

    @Test
    void shouldPassValidationWhenFilmDurationIsOneForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1) // минимальное положительное значение
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Фильм с продолжительностью 1 должен быть валидным для создания");
    }

    // Тесты для Film - обновление

    @Test
    void shouldFailValidationWhenFilmIdIsNullForUpdate() {
        Film film = Film.builder()
                .id(null) // ID обязателен для обновления
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);

        assertFalse(violations.isEmpty(), "Фильм с null ID должен быть невалидным для обновления");
    }

    @Test
    void shouldPassValidationWhenFilmNameIsBlankForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("") // пустое название допустимо для обновления
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);

        // Проверяем, что нет нарушений связанных с @NotBlank (только с @Size если есть)
        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNotBlankViolation, "Пустое название должно быть допустимо для обновления");
    }

    // Тесты для User - создание

    @Test
    void shouldFailValidationWhenUserEmailIsBlankForCreation() {
        User user = User.builder()
                .email("") // пустой email
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с пустым email должен быть невалидным для создания");
    }

    @Test
    void shouldFailValidationWhenUserEmailHasNoAtSymbolForCreation() {
        User user = User.builder()
                .email("invalid-email.com") // email без @
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с email без @ должен быть невалидным для создания");
    }

    @Test
    void shouldFailValidationWhenUserLoginIsBlankForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("") // пустой логин
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с пустым логином должен быть невалидным для создания");
    }

    @Test
    void shouldFailValidationWhenUserLoginContainsSpacesForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("login with spaces") // логин с пробелами
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим пробелы должен быть невалидным для создания");
    }

    @Test
    void shouldFailValidationWhenUserLoginIsTooShortForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abc") // 3 символа - меньше минимума
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с логином короче 4 символов должен быть невалидным для создания");
    }

    @Test
    void shouldPassValidationWhenUserLoginIsExactlyMinLengthForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abcd") // 4 символа - граничное значение
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 4 символа должен быть валидным для создания");
    }

    @Test
    void shouldFailValidationWhenUserLoginIsTooLongForCreation() {
        String longLogin = "a".repeat(21); // 21 символ - больше максимума
        User user = User.builder()
                .email("valid@email.com")
                .login(longLogin)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с логином длиннее 20 символов должен быть невалидным для создания");
    }

    @Test
    void shouldPassValidationWhenUserLoginIsExactlyMaxLengthForCreation() {
        String maxLengthLogin = "a".repeat(20); // 20 символов - граничное значение
        User user = User.builder()
                .email("valid@email.com")
                .login(maxLengthLogin)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 20 символов должен быть валидным для создания");
    }

    @Test
    void shouldFailValidationWhenUserBirthdayIsInFutureForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.now().plusDays(1)) // дата в будущем
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с датой рождения в будущем должен быть невалидным для создания");
    }

    @Test
    void shouldPassValidationWhenUserBirthdayIsTodayForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.now()) // граничное значение
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Пользователь с сегодняшней датой рождения должен быть валидным для создания");
    }

    @Test
    void shouldPassValidationWhenUserNameIsNullForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name(null) // имя может быть null
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Пользователь с null именем должен быть валидным для создания");
    }

    @Test
    void shouldPassValidationWhenUserNameIsBlankForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name("") // имя может быть пустым
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty(), "Пользователь с пустым именем должен быть валидным для создания");
    }

    // Тесты для User - обновление

    @Test
    void shouldFailValidationWhenUserIdIsNullForUpdate() {
        User user = User.builder()
                .id(null) // ID обязателен для обновления
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);

        assertFalse(violations.isEmpty(), "Пользователь с null ID должен быть невалидным для обновления");
    }

    @Test
    void shouldPassValidationWhenUserEmailIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("") // пустой email допустим для обновления
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);

        // Проверяем, что нет нарушений связанных с @NotBlank
        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNotBlankViolation, "Пустой email должен быть допустим для обновления");
    }

    @Test
    void shouldPassValidationWhenUserLoginIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("") // пустой логин допустим для обновления
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);

        // Проверяем, что нет нарушений связанных с @NotBlank
        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNotBlankViolation, "Пустой логин должен быть допустим для обновления");
    }
}