package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @Autowired
    public FilmController(InMemoryFilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    //вывод всех фильмов
    @GetMapping
    public Collection<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    //вывод фильма по id
    @GetMapping("/{id}")
    public Film getFilmById(@Positive @PathVariable int id) {
        return filmStorage.findFilmById(id);
    }

    //Добавить фильм
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmStorage.createFilm(film);
    }

    //Обновить фильм
    @PutMapping
    public Film update(@RequestBody Film updatedFilm) {
        return filmStorage.update(updatedFilm);
    }

    //вывод 10 популярных фильмов
    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@Positive @RequestParam(defaultValue = "10") int count) {
        return filmService.getMostPopularFilms(count);
    }

    // Поставить лайк фильму
    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@Positive @PathVariable Long filmId, @Positive @PathVariable Long userId) {
        filmService.addLike(filmId, userId);
    }

    // Удалить лайк у фильма
    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@Positive @PathVariable Long filmId, @Positive @PathVariable Long userId) {
        filmService.removeLike(filmId, userId);
    }
}


