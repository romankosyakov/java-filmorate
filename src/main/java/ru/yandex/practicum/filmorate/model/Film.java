package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
@AllArgsConstructor
public class Film {
    @NotNull(groups = UpdateValidation.class, message = "ID фильма обязателен для обновления")
    private final Integer id;

    @NotBlank(message = "Название фильма не может быть пустым", groups = CreateValidation.class)
    @Size(max = 100, message = "Название фильма не может быть длиннее 100 символов",
            groups = {CreateValidation.class, UpdateValidation.class})
    private final String name;

    @Size(max = 200, message = "Описание фильма не может быть длиннее 200 символов",
            groups = {CreateValidation.class, UpdateValidation.class})
    private String description;

    @NotNull(message = "Дата релиза обязательна", groups = CreateValidation.class)
    @MinReleaseDate(groups = {CreateValidation.class, UpdateValidation.class})
    private final LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом",
            groups = {CreateValidation.class, UpdateValidation.class})
    private final Integer duration;
}