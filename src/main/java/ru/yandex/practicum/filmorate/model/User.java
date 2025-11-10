package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class User {
    public static final String LOGIN_PATTERN = "^\\w+$";
    public static final int LOGIN_MIN_LENGTH = 4;
    public static final int LOGIN_MAX_LENGTH = 20;

    private final Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "ЛогФин не может быть пустым")
    @Size(min = LOGIN_MIN_LENGTH, max = LOGIN_MAX_LENGTH,
            message = "Логин должен быть от 4 до 20 символов")
    @Pattern(regexp = LOGIN_PATTERN,
            message = "Логин может содержать только латинские буквы, цифры и символ подчеркивания")
    private final String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;

    public String getName() {
        return name == null || name.isBlank() ? login : name;
    }
}