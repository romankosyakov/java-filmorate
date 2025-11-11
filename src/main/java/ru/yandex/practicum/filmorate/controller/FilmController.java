package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Вызван метод получения списка всех фильмов");
        return List.copyOf(films.values());
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.debug("Вызван метод получения фильма с ID: {}", id);
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addNewFilm(@Valid @RequestBody Film film) {
        validateFilm(film);
        Film newFilm = Film.builder()
                .id(id++)
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм: '{}' (ID: {})", newFilm.getName(), newFilm.getId());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film filmUpdate) {
        if (filmUpdate.getId() == null) {
            throw new ValidationException("ID фильма обязателен для обновления");
        }

        Film existingFilm = films.get(filmUpdate.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с ID " + filmUpdate.getId() + " не найден");
        }

        Film updatedFilm = Film.builder()
                .id(existingFilm.getId())
                .name(filmUpdate.getName() != null ? filmUpdate.getName() : existingFilm.getName())
                .description(filmUpdate.getDescription() != null ? filmUpdate.getDescription() : existingFilm.getDescription())
                .releaseDate(filmUpdate.getReleaseDate() != null ? filmUpdate.getReleaseDate() : existingFilm.getReleaseDate())
                .duration(filmUpdate.getDuration() != null ? filmUpdate.getDuration() : existingFilm.getDuration())
                .build();

        validateFilm(updatedFilm);
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлен фильм: '{}' (ID: {})", updatedFilm.getName(), updatedFilm.getId());

        return updatedFilm;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}