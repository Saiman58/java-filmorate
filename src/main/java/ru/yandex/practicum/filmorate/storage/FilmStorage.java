package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> findAllFilms();


    Film createFilm(Film film) throws ValidationException;


    Film update(Film updatedFilm) throws ValidationException;

    //  поиск фильма по его идентификатору
    Film findFilmById(long userId);


}
