package ru.yandex.practicum.filmorate.storage;

public interface LikeStorage {
    void addLike(int filmId, long userId);

    void removeLike(int filmId, long userId);
}
