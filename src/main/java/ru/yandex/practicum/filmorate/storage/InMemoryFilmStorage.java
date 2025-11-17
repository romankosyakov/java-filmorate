package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    public Film getFilm(int id) {
        if (id <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    public List<Film> getAllFilms() {
        return List.copyOf(films.values());
    }

    public Film addNewFilm(@Validated(CreateValidation.class) Film film) {
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

    public Film updateFilm(@Validated(UpdateValidation.class) Film filmUpdate) {
        Film existingFilm = films.get(filmUpdate.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с ID " + filmUpdate.getId() + " не найден");
        }

        // Сохраняем лайки из существующего фильма
        Set<Long> existingLikes = existingFilm.getFilmLikes();

        Film updatedFilm = Film.builder()
                .id(existingFilm.getId())
                .name(filmUpdate.getName() != null ? filmUpdate.getName() : existingFilm.getName())
                .description(filmUpdate.getDescription() != null ? filmUpdate.getDescription() : existingFilm.getDescription())
                .releaseDate(filmUpdate.getReleaseDate() != null ? filmUpdate.getReleaseDate() : existingFilm.getReleaseDate())
                .duration(filmUpdate.getDuration() != null ? filmUpdate.getDuration() : existingFilm.getDuration())
                .build();

        // Восстанавливаем лайки
        updatedFilm.getFilmLikes().addAll(existingLikes);

        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлен фильм: '{}' (ID: {})", updatedFilm.getName(), updatedFilm.getId());

        return updatedFilm;
    }

    public void deleteFilm(int filmID) {
        if (filmID <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        Film existingFilm = films.get(filmID);
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с ID " + filmID + " не найден");
        }
        films.remove(filmID);
        log.info("Удален фильм с ID {}", filmID);
    }

    public void deleteAllFilms() {
        if (films.isEmpty()) {
            throw new NotFoundException("Список фильмов пуст. Невозможно выполнить операцию");
        }
        films.clear();
        log.info("Список фильмов пуст, выполнена процедура очистки списка фильмов.");
    }
}
