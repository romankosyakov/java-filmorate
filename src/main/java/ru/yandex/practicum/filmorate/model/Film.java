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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class Film {
    @NotNull(groups = UpdateValidation.class, message = "ID фильма обязателен для обновления")
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым", groups = CreateValidation.class)
    @Size(max = 100, message = "Название фильма не может быть длиннее 100 символов",
            groups = {CreateValidation.class, UpdateValidation.class})
    private String name;

    @Size(max = 200, message = "Описание фильма не может быть длиннее 200 символов",
            groups = {CreateValidation.class, UpdateValidation.class})
    private String description;

    @NotNull(message = "Дата релиза обязательна", groups = CreateValidation.class)
    @MinReleaseDate(groups = {CreateValidation.class, UpdateValidation.class})
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом",
            groups = {CreateValidation.class, UpdateValidation.class})
    private Integer duration;

    @NotNull(message = "Рейтинг MPA обязателен", groups = CreateValidation.class)
    private Mpa mpa;

    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @Builder.Default
    private final Set<Long> likes = new HashSet<>();

    public int getRate() {
        return likes.size();
    }
}