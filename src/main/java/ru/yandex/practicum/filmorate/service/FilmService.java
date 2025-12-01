package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final LikeDbStorage likeDbStorage;

    public void putLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);
        try {
            likeDbStorage.addLike(filmID, userID);
            log.info("Пользователь с ID {} поставил лайк фильму с ID {}.", userID, filmID);
        } catch (DataIntegrityViolationException e) {
            log.debug("Лайк от пользователя {} фильму {} уже существует", userID, filmID);
            throw new ValidationException("Пользователь с ID " + userID + " уже поставил лайк этому фильму.");
        }
    }

    public void deleteLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);

        likeDbStorage.removeLike(filmID, userID);
        log.info("Пользователь с ID {} удалил лайк фильму с ID {}.", userID, filmID);
    }

    public List<Film> showMostLikedFilms(Integer countToShow) {
        return filmDbStorage.getMostLikedFilms(countToShow);
    }

    private void validateFilmAndUser(int filmID, long userID) {
        Film film = filmDbStorage.getFilm(filmID);
        User user = userDbStorage.getUser(userID);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден.");
        }
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmID + " не найден.");
        }
    }

}