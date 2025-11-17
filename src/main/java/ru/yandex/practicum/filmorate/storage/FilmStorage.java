package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

 public interface FilmStorage {
     Film getFilm(int id);

     List<Film> getAllFilms();

     Film addNewFilm(Film film);

     Film updateFilm(Film filmUpdate);

     void deleteFilm(int id);

     void deleteAllFilms();
}
