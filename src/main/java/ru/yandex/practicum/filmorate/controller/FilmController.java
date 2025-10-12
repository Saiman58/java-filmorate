package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmServiceImp;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmServiceImp filmService;

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Запрос на создание фильма: {}", film);
        Film createdFilm = filmService.createFilm(film);
        log.info("Фильм создан: {}", film);
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Запрос на обновление фильма: {}", film);
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        // BREAKPOINT 1: Проверяем входящие параметры
        log.info("Добавление лайка: фильм {}, пользователь {}", id, userId);
        System.out.println("DEBUG Controller: filmId=" + id + ", userId=" + userId);

        filmService.addLike(id, userId);
    }


    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка: фильм {}, пользователь {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с ID: {}", id);
        return filmService.getFilmById(id);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }
}