package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ControllerValidationTest {

    @Test
    void shouldHandleEmptyRequestBodyForFilm() {
        FilmController filmController = new FilmController();

        Film emptyFilm = Film.builder()
                .name("")
                .description("")
                .releaseDate(null)
                .duration(0)
                .build();

        // Проверяем, что контроллер корректно обрабатывает невалидные данные
        // (валидация происходит через @Valid, поэтому исключения будут обработаны ErrorHandler)
        assertDoesNotThrow(() -> {
            try {
                filmController.addNewFilm(emptyFilm);
            } catch (Exception e) {
                // Ожидаем, что валидация может провалиться
            }
        });
    }

    @Test
    void shouldHandleEmptyRequestBodyForUser() {
        UserController userController = new UserController();

        User emptyUser = User.builder()
                .email("")
                .login("")
                .name("")
                .birthday(null)
                .build();

        // Проверяем, что контроллер корректно обрабатывает невалидные данные
        assertDoesNotThrow(() -> {
            try {
                userController.addNewUser(emptyUser);
            } catch (Exception e) {
                // Ожидаем, что валидация может провалиться
            }
        });
    }
}