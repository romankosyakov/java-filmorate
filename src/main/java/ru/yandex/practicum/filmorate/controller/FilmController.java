package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final int defaultCount;

    public FilmController(@Value("${mostLikedCount}") int defaultCount, FilmService filmService) {
        this.defaultCount = defaultCount;
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Вызван метод получения списка всех фильмов");
        return filmService.getFilmStorage().getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.debug("Вызван метод получения фильма с ID: {}", id);
        return filmService.getFilmStorage().getFilm(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addNewFilm(@Validated(CreateValidation.class) @RequestBody Film film) {
        return filmService.getFilmStorage().addNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated(UpdateValidation.class) @RequestBody Film filmUpdate) {
        return filmService.getFilmStorage().updateFilm(filmUpdate);
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
            @RequestParam(value = "count", defaultValue = "${mostLikedCount}", required = false) Integer countToShow) {
        if (countToShow == null || countToShow <= 0) {
            countToShow = defaultCount;
        }
        return filmService.showMostLikedFilms(countToShow);
    }
}