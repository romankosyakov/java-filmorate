package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    // Конструктор для простых ошибок
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Конструктор для ошибок валидации
    public ErrorResponse(String error, String message, Map<String, String> details) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }
}