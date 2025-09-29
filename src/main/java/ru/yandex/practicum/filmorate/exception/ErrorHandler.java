package ru.yandex.practicum.filmorate.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    // исключений валидации 400
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка: 400 ", e.getMessage());
    }

    // исключений валидации 400, некорректные данные от пользователя
    /* @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.warn("Некорректные данные от пользователя: {}", errorMessage);
        return new ErrorResponse("Некорректные данные", errorMessage);
    }*/

    //  исключение когда объект не найден 404
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return new ErrorResponse("Ошибка: 404 ", e.getMessage());
    }

    // все остальные исключения 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception e) {
        log.error("Ошибка: 500,   {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка: 500 ", e.getMessage());
    }
}
