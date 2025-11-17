package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public void putLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);
        Film film = filmStorage.getFilm(filmID);
        Set<Long> filmLikes = film.getFilmLikes();
        if (filmLikes.contains(userID)) {
            throw new ValidationException("Пользователь с ID " + userID + " уже поставил лайк этому фильму.");
        } else {
            filmLikes.add(userID);
            log.info("Пользователь с ID {} поставил лайк фильму с ID {}.", userID, filmID);
        }
    }

    public void deleteLike(int filmID, long userID) {
        validateFilmAndUser(filmID, userID);
        Film film = filmStorage.getFilm(filmID);
        Set<Long> filmLikes = film.getFilmLikes();
        if (!filmLikes.contains(userID)) {
            throw new ValidationException("Пользователь с ID " + userID + " не ставил лайк этому фильму.");
        } else {
            filmLikes.remove(userID);
            log.info("Пользователь с ID {} удалил лайк фильму с ID {}.", userID, filmID);
        }
    }

    public List<Film> showMostLikedFilms(int countToShow) {
        List<Film> films = filmStorage.getAllFilms();
        if (films.isEmpty()) {
            throw new NotFoundException("В коллекции отсутствуют фильмы.");
        }

        return films.stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getFilmLikes().size();
                    int likes2 = f2.getFilmLikes().size();

                    // Если у обоих фильмов есть лайки, сортируем по количеству лайков (убывание)
                    if (likes1 > 0 && likes2 > 0) {
                        return Integer.compare(likes2, likes1);
                    } else if (likes1 > 0) {
                        // Если лайки только у первого фильма - он должен быть выше
                        return -1;
                    } else if (likes2 > 0) {
                        // Если лайки только у второго фильма - он должен быть выше
                        return 1;
                    } else {
                        // Если у обоих нет лайков - сортируем по названию (алфавит)
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                })
                .limit(countToShow)
                .collect(Collectors.toList());
    }

    private void validateFilmAndUser(int filmID, long userID) {
        Film film = filmStorage.getFilm(filmID);
        User user = userStorage.getUser(userID);
        if (filmID <= 0 || userID <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден.");
        }
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmID + " не найден.");
        }
    }
}