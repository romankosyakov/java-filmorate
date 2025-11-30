package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {

    private final MpaDbStorage mpaDbStorage;

    @GetMapping
    public List<Mpa> getAllMpa() {
        log.debug("Вызван метод получения всех рейтингов MPA");
        return mpaDbStorage.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        log.debug("Вызван метод получения рейтинга MPA с ID: {}", id);
        return mpaDbStorage.getMpaById(id);
    }
}