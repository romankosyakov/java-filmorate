package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Вызван метод получения списка всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.debug("Вызван метод получения фильма с ID: {}", id);
        return filmService.getFilm(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addNewFilm(@Validated(CreateValidation.class) @RequestBody Film film) {
        log.debug("Вызван метод добавления нового фильма: {}", film.getName());
        return filmService.addNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated(UpdateValidation.class) @RequestBody Film film) {
        log.debug("Вызван метод обновления фильма с ID: {}", film.getId());
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable int id, @PathVariable long userId) {
        log.debug("Пользователь {} поставил лайк фильму {}", userId, id);
        filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable long userId) {
        log.debug("Пользователь {} удалил лайк фильму {}", userId, id);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.debug("Вызван метод получения {} популярных фильмов", count);
        return filmService.showMostLikedFilms(count);
    }
}