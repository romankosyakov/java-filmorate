package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    public Film getFilm(int id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    public List<Film> getAllFilms() {
        return List.copyOf(films.values());
    }

    public Film addNewFilm(Film film) {
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

    public Film updateFilm(Film filmUpdate) {
        Film existingFilm = films.get(filmUpdate.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с ID " + filmUpdate.getId() + " не найден");
        }

        if (filmUpdate.getDescription() != null) {
            existingFilm.setDescription(filmUpdate.getDescription());
        }
        if (filmUpdate.getDuration() != null) {
            existingFilm.setDuration(filmUpdate.getDuration());
        }
        if (filmUpdate.getName() != null) {
            existingFilm.setName(filmUpdate.getName());
        }
        if (filmUpdate.getReleaseDate() != null) {
            existingFilm.setReleaseDate(filmUpdate.getReleaseDate());
        }

        log.info("Обновлен фильм: '{}' (ID: {})", existingFilm.getName(), existingFilm.getId());
        return existingFilm;
    }

}
