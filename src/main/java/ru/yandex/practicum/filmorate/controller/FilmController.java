package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    //вывод всех фильмов
    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    //Добавить фильм
    @PostMapping
    public Film create(@RequestBody Film film) {
        //уловия
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная количество символов 200");
        }

        LocalDate startingDate = LocalDate.of(1895, 12, 28); //рождение кино
        LocalDate currentDate = LocalDate.now(); //тек. дата
        if (film.getReleaseDate().isBefore(startingDate) || film.getReleaseDate().isAfter(currentDate)) {
            throw new ValidationException("Дата релиза фильма не может быть раньше дня рождения кино");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность не может быть равна или маньше 0");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
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

    //обновить фильм
    @PutMapping
    public Film update(@RequestBody Film updatedFilm) {
        //поиск по id
        if (updatedFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(updatedFilm.getId())) {
            throw new ValidationException("Фильм с id = " + updatedFilm.getId() + " не найден");
        }

        //обновление
        Film existingFilm = films.get(updatedFilm.getId());
        //название
        if (updatedFilm.getName() != null || updatedFilm.getName().isBlank()) {
            existingFilm.setName(updatedFilm.getName());
        }
        //описание
        if (updatedFilm.getDescription() != null && updatedFilm.getDescription().length() <= 200) {
            existingFilm.setDescription(updatedFilm.getDescription());
        }
        //дата релиза
        LocalDate startingDate = LocalDate.of(1895, 12, 28); //рождение кино
        LocalDate currentDate = LocalDate.now(); //тек. дата
        LocalDate releaseDate = updatedFilm.getReleaseDate();

        if (releaseDate != null) {
            if (releaseDate.isBefore(startingDate) || releaseDate.isAfter(currentDate)) {
                existingFilm.setReleaseDate(releaseDate);
            }
        }

        if (updatedFilm.getDuration() != null && updatedFilm.getDuration() <= 0) {
            existingFilm.setDuration(updatedFilm.getDuration());
        }
        return existingFilm;
    }

}


