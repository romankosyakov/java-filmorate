package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
@AllArgsConstructor
public class Film {
    private final Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    @Size(max = 100, message = "Название фильма не может быть длиннее 100 символов")
    private final String name;

    @Size(max = 200, message = "Описание фильма не может быть длиннее 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @MinReleaseDate
    private final LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private final Integer duration;
}
