package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    public void putLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);

        Film film = filmDbStorage.getFilm(filmID);
        if (film.getLikes().contains(userID)) {
            throw new ValidationException("Пользователь с ID " + userID + " уже поставил лайк этому фильму.");
        } else {
            filmDbStorage.addLike(filmID, userID);
            log.info("Пользователь с ID {} поставил лайк фильму с ID {}.", userID, filmID);
        }
    }

    public void deleteLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);

        Film film = filmDbStorage.getFilm(filmID);
        if (!film.getLikes().contains(userID)) {
            throw new ValidationException("Пользователь с ID " + userID + " не ставил лайк этому фильму.");
        } else {
            filmDbStorage.removeLike(filmID, userID);
            log.info("Пользователь с ID {} удалил лайк фильму с ID {}.", userID, filmID);
        }
    }

    public List<Film> showMostLikedFilms(Integer countToShow) {
        List<Film> films = filmDbStorage.getAllFilms();
        return films.stream()
                .sorted(Comparator.comparingLong(Film::getRate).reversed())
                .limit(countToShow)
                .collect(Collectors.toList());
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