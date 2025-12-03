package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {
    Film getFilm(int id);

    List<Film> getAllFilms();

    Map<Integer, List<Genre>> getGenresForFilms(List<Integer> filmIds);

    Map<Integer, Set<Long>> getLikesForFilms(List<Integer> filmIds);

    Film addNewFilm(Film film);

    Film updateFilm(Film filmUpdate);

    List<Genre> processGenres(List<Genre> genres);

    void addGenresToFilm(int filmId, List<Genre> genres);

    List<Film> getMostLikedFilms(int count);
}