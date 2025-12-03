package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final MpaStorage mpaStorage;

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void putLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);
        try {
            likeStorage.addLike(filmID, userID);
            log.info("Пользователь с ID {} поставил лайк фильму с ID {}.", userID, filmID);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Пользователь с ID " + userID + " уже поставил лайк этому фильму.");
        }
    }

    public void deleteLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);
        likeStorage.removeLike(filmID, userID);
        log.info("Пользователь с ID {} удалил лайк фильму с ID {}.", userID, filmID);
    }

    public List<Film> showMostLikedFilms(Integer countToShow) {
        return filmStorage.getMostLikedFilms(countToShow);
    }

    @Transactional
    public Film addNewFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA rating is required");
        }

        Mpa fullMpa = mpaStorage.getMpaById(film.getMpa().getId());
        film.setMpa(fullMpa);

        List<Genre> processedGenres = filmStorage.processGenres(film.getGenres());

        Film savedFilm = filmStorage.addNewFilm(film);
        filmStorage.addGenresToFilm(savedFilm.getId(), processedGenres);

        Film resultFilm = filmStorage.getFilm(savedFilm.getId());
        resultFilm.setMpa(fullMpa);
        resultFilm.setGenres(processedGenres);

        log.info("Добавлен новый фильм: '{}' (ID: {})", resultFilm.getName(), resultFilm.getId());
        return resultFilm;
    }

    @Transactional
    public Film updateFilm(Film filmUpdate) {
        Film existingFilm = filmStorage.getFilm(filmUpdate.getId());

        if (filmUpdate.getMpa() == null || filmUpdate.getMpa().getId() == null) {
            throw new ValidationException("MPA rating is required");
        }

        Mpa fullMpa = mpaStorage.getMpaById(filmUpdate.getMpa().getId());
        filmUpdate.setMpa(fullMpa);

        List<Genre> processedGenres;
        if (filmUpdate.getGenres() != null) {
            processedGenres = filmStorage.processGenres(filmUpdate.getGenres());
        } else {
            processedGenres = existingFilm.getGenres();
        }

        Film updatedFilm = filmStorage.updateFilm(filmUpdate);
        filmStorage.addGenresToFilm(updatedFilm.getId(), processedGenres);

        Film resultFilm = filmStorage.getFilm(updatedFilm.getId());
        resultFilm.setMpa(fullMpa);
        resultFilm.setGenres(processedGenres);

        log.info("Обновлен фильм: '{}' (ID: {})", resultFilm.getName(), resultFilm.getId());
        return resultFilm;
    }

    private void validateFilmAndUser(int filmID, long userID) {
        Film film = filmStorage.getFilm(filmID);
        User user = userStorage.getUser(userID);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден.");
        }
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmID + " не найден.");
        }
    }
}