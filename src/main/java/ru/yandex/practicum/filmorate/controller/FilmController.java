package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

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
    public Film addNewFilm(@Validated(CreateValidation.class) @RequestBody Film film) {
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
    public Film updateFilm(@Validated(UpdateValidation.class) @RequestBody Film filmUpdate) {
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

        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлен фильм: '{}' (ID: {})", updatedFilm.getName(), updatedFilm.getId());

        return updatedFilm;
    }
}