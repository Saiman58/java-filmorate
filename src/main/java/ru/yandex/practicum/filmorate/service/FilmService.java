package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;


@Service
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    // Добавление лайка к фильму
    public void addLike(Long filmId, Long userId) {
        log.info("Попытка добавить лайк от пользователя:  {} фильму: {}", userId, filmId);
        Film film = filmStorage.findFilmById(filmId);
        User user = userStorage.findUserById(userId);

        if (film == null) {
            log.error("Фильм не найден: filmId={}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }

        if (user == null) {
            log.error("Пользователь не найден: userId={}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        Set<Long> likes = filmLikes.get(filmId);
        if (likes != null && likes.contains(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь с id = " + userId + " уже поставил лайк фильму с id = " + filmId);
        }

        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    // Удаление лайка у фильма
    public void removeLike(Long filmId, Long userId) {
        log.info("Попытка удалить лайка у фильма");
        Set<Long> likes = filmLikes.get(filmId);
        if (likes == null || !likes.remove(userId)) {
            log.error("Лайк пользователя {} к фильму {} не найден", userId, filmId);
            throw new NotFoundException("Лайк пользователя не найден");
        }
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    // Получение 10 наиболее популярных фильмов по количеству лайков
    public List<Film> getMostPopularFilms(int count) {
        log.info("Попытка получение 10 наиболее популярных фильмов по количеству лайков");
        List<Film> mostPopularFilms = new ArrayList<>();
        List<Map.Entry<Long, Set<Long>>> sortedEntries = new ArrayList<>(filmLikes.entrySet());

        // Сортировка по количеству лайков
        sortedEntries.sort((entry1, entry2) -> Integer.compare(
                entry2.getValue().size(), entry1.getValue().size()));

        // 10 самых популярных фильмов
        for (int i = 0; i < Math.min(count, sortedEntries.size()); i++) {
            Long filmId = sortedEntries.get(i).getKey();
            Film film = filmStorage.findFilmById(filmId);
            if (film != null) {
                mostPopularFilms.add(film);
            }
        }
        return mostPopularFilms;
    }
}
