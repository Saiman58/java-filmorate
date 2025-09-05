package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final static Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    //вывод всех фильмов
    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    //Добавить фильм
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        try {
            //Проверка на пустое описание
            if (film.getDescription() == null || film.getDescription().length() > 200) {
                log.error("Максимальная количество символов 200");
                throw new ValidationException("Максимальная количество символов: 200");
            }

            //Проверка даты релиза
            LocalDate startingDate = LocalDate.of(1895, 12, 28); //рождение кино
            LocalDate currentDate = LocalDate.now(); //тек. дата
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(startingDate) ||
                    film.getReleaseDate().isAfter(currentDate)) {
                log.error("Дата релиза фильма не может быть раньше дня рождения кино");
                throw new ValidationException("Дата релиза фильма не может быть раньше дня рождения кино");
            }
            //Проверка продолжительности фильма
            if (film.getDuration() <= 0) {
                log.error("Продолжительность не может быть равна или меньше 0");
                throw new ValidationException("Продолжительность не может быть равна или меньше 0");
            }

            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Фильм успешно создан: {}", film);
            return film;
        } catch (ValidationException e) {
            log.error("Создание фильма завершилось ошибкой", e);
            throw e;
        } catch (Exception e) {
            log.error("Произошла непредвиденная ошибка", e);
            throw new RuntimeException("Произошла непредвиденная ошибка", e);
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    //Обновить фильм
    @PutMapping
    public Film update(@RequestBody Film updatedFilm) {
        //Проверка, что ID фильма указан
        if (updatedFilm.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        //Проверка, существует ли фильм с указанным ID
        if (!films.containsKey(updatedFilm.getId())) {
            log.error("Фильм с id = {} не найден", updatedFilm.getId());
            throw new ValidationException("Фильм с id = " + updatedFilm.getId() + " не найден");
        }

        Film existingFilm = films.get(updatedFilm.getId());

        // Обновление названия фильма
        if (updatedFilm.getName() != null || updatedFilm.getName().isBlank()) {
            existingFilm.setName(updatedFilm.getName());
            log.info("Название фильма обновлено");
        }

        //Обновление описания фильма
        if (updatedFilm.getDescription() != null && updatedFilm.getDescription().length() <= 200) {
            existingFilm.setDescription(updatedFilm.getDescription());
            log.info("Описание фильма обновлено");
        }

        //Обновление даты релиза фильма
        LocalDate startingDate = LocalDate.of(1895, 12, 28); //рождение кино
        LocalDate currentDate = LocalDate.now(); //тек. дата
        LocalDate releaseDate = updatedFilm.getReleaseDate();
        if (releaseDate != null) {
            if (!(releaseDate.isBefore(startingDate) || releaseDate.isAfter(currentDate))) {
                existingFilm.setReleaseDate(releaseDate);
                log.info("Дата релиза фильма обновлена");
            } else {
                log.error("Дата релиза фильма должна быть между {} и {}", startingDate, currentDate);
            }
        }

        // Обновление продолжительности фильма
        if (updatedFilm.getDuration() != null && updatedFilm.getDuration() > 0) {
            existingFilm.setDuration(updatedFilm.getDuration());
            log.info("Продолжительность фильма обновлено");
        }
        return existingFilm;
    }

}


