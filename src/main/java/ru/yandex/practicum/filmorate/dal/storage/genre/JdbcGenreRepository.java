package ru.yandex.practicum.filmorate.dal.storage.genre;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {
    RowMapper<Genre> mapper = new GenreRowMapper();
    JdbcTemplate jdbc;

    @Override
    public Optional<Genre> getGenreById(int genreId) {
        Optional<Genre> genre = Optional.ofNullable(jdbc.queryForObject("SELECT * FROM genre WHERE genre_id = ?",
                mapper, genreId));
        return genre;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        log.debug("getAll().");
        List<Genre> result = jdbc.query("SELECT * FROM genre ORDER BY genre_id", mapper);
        log.trace("Возвращены все жанры: {}.", result);
        return result;
    }
}