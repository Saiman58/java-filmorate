package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
@Component
public class FilmValidator {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    public void validateFilm(Film film) {
        validateName(film.getName());
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            log.warn("Попытка создания фильма без названия");
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    public void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Попытка создания фильма с превышением допустимой длины: {} символов", description.length());
            throw new ValidationException("Описание не может быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
        }
    }

    public void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            log.warn("Дата релиза не указана");
            throw new ValidationException("Дата релиза обязательна");
        }
        if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            log.warn("Попытка создания фильма с некорректной датой: {}", releaseDate);
            throw new ValidationException("Дата релиза — не раньше " + MIN_RELEASE_DATE);
        }
    }

    public void validateDuration(long duration) {
        if (duration <= 0) {
            log.warn("Попытка создания фильма c некорректной продолжительностью: {}", duration);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public void validateIdForUpdate(Long id) {
        if (id == null || id == 0) {
            log.warn("Попытка обновления фильма без ID");
            throw new ValidationException("ID фильма обязателен для обновления");
        }
    }
}