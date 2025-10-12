package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmService {
    public Film createFilm(Film film);

    public Film updateFilm(Film film);

    public Film getFilmById(Long filmId);

    public List<Film> getFilms();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(int count);
}
