package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Slf4j
public class GenreController {

    private final GenreDbStorage genreDbStorage;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.debug("Вызван метод получения всех жанров");
        return genreDbStorage.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.debug("Вызван метод получения жанра с ID: {}", id);
        return genreDbStorage.getGenreById(id);
    }
}