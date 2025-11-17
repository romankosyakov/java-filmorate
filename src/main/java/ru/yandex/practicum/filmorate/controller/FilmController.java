package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@AllArgsConstructor
@ConfigurationProperties
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Вызван метод получения списка всех фильмов");
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.debug("Вызван метод получения фильма с ID: {}", id);
        return filmStorage.getFilm(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addNewFilm(@Validated(CreateValidation.class) @RequestBody Film film) {
        return filmStorage.addNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated(UpdateValidation.class) @RequestBody Film filmUpdate) {
        return filmStorage.updateFilm(filmUpdate);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable("id") int filmID, @PathVariable("userId") long userID) {
        filmService.putLike(filmID, userID);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int filmID, @PathVariable("userId") long userID) {
        filmService.deleteLike(filmID, userID);
    }

    @GetMapping("/popular")
    public List<Film> showMostLikedFilms(
            @RequestParam(value = "count", required = false) Integer countToShow,
            @Value("${mostLikedCount}") int defaultCount) {
        return filmService.showMostLikedFilms((countToShow != null) ? countToShow : defaultCount);
    }
}