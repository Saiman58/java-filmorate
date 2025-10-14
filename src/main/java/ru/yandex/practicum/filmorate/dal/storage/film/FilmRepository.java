package ru.yandex.practicum.filmorate.dal.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmRepository extends FilmStorage {
    Film create(Film film);

    Film getById(Long id);

    Map<Long, Film> getAll();

    List<Film> getAllValues();

    Film update(Film film);

    void deleteById(Long id);

    Set<Long> getLikeUserIdsFromDB(long id);

    List<Genre> getGenreFromDB(long id);

    List<Film> getPopularFilms(int count);

    int getLikeCount(Film film);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

}