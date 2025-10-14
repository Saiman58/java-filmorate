package ru.yandex.practicum.filmorate.dal.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreRepository {
    Optional<Genre> getGenreById(int genreId);

    Collection<Genre> getAllGenres();
}
