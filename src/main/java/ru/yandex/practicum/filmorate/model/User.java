package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class User {
    public static final String LOGIN_PATTERN = "^\\w+$";
    public static final int LOGIN_MIN_LENGTH = 4;
    public static final int LOGIN_MAX_LENGTH = 20;

    @NotNull(groups = UpdateValidation.class, message = "ID пользователя обязателен для обновления")
    private final Long id;

    @NotBlank(message = "Email не может быть пустым", groups = CreateValidation.class)
    @Email(message = "Некорректный email", groups = {CreateValidation.class, UpdateValidation.class})
    private String email;

    @NotBlank(message = "Логин не может быть пустым", groups = CreateValidation.class)
    @Size(min = LOGIN_MIN_LENGTH, max = LOGIN_MAX_LENGTH,
            message = "Логин должен быть от 4 до 20 символов", groups = {CreateValidation.class, UpdateValidation.class})
    @Pattern(regexp = LOGIN_PATTERN,
            message = "Логин может содержать только латинские буквы, цифры и символ подчеркивания",
            groups = {CreateValidation.class, UpdateValidation.class})
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем",
            groups = {CreateValidation.class, UpdateValidation.class})
    private LocalDate birthday;

    public String getName() {
        return name == null || name.isBlank() ? login : name;
    }
}