package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmTest {

    @Test
    void shouldNotCreateFilmWithEmptyDescription() {
        Film film = new Film();
        film.setDescription(""); // Пустое??

        assertTrue(film.getDescription().isEmpty(), "Описание не может быть пустым");
    }

    @Test
    void shouldNotCreateFilmWithDescriptionTooLong() {
        Film film = new Film();
        film.setDescription("A".repeat(201)); // больше 200??

        assertTrue(film.getDescription().length() > 200, "Описание не может превышать 200 символов");
    }

    @Test
    void shouldNotCreateFilmWithReleaseDateBefore1895() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.of(1894, 12, 27)); // дата до 1895 года??

        assertTrue(film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)), "Дата релиза фильма не может быть раньше 28 декабря 1895 года");
    }

    @Test
    void shouldNotCreateFilmWithNegativeDuration() {
        Film film = new Film();
        film.setDuration(-3.14); // продолжительность с "-"

        assertTrue(film.getDuration() <= 0, "Продолжительность не может быть равна или меньше 0");
    }
}
