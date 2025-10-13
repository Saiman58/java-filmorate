package ru.yandex.practicum.filmorate.dal.storage.film;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

@Repository("jdbcFilmRepository")
@Primary
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JdbcFilmRepository extends BaseRepository<Film> implements FilmStorage {
    RowMapper<Film> mapper = new FilmRowMapper();

    public JdbcFilmRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Film createFilm(Film film) {
        validateMpaExists(film.getMpa().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenresExist(film.getGenres());
        }

        String query = "INSERT INTO film(name, description, release_date, duration_in_minutes, rating_id) VALUES (?, ?, ?, ?, ?)";
        long id = super.create(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film.getId(), film.getGenres());
        }

        return film;
    }

    @Override
    public Film getFilmById(Long id) {
        String query = "SELECT f.*, r.name as mpa_name FROM film f " +
                "JOIN rating_mpa r ON f.rating_id = r.rating_id " +
                "WHERE f.film_id = ?";
        Film film = findOne(query, mapper, id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден.", id)));

        loadFilmGenres(film);

        return film;
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT f.*, r.name as mpa_name FROM film f " +
                "JOIN rating_mpa r ON f.rating_id = r.rating_id";
        List<Film> films = findMany(query, mapper);

        films.forEach(this::loadFilmGenres);

        return films;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());

        validateMpaExists(film.getMpa().getId());

        if (film.getGenres() != null) {
            validateGenresExist(film.getGenres());
        }

        String query = "UPDATE film SET name = ?, description = ?, release_date = ?, duration_in_minutes = ?, " +
                "rating_id = ? WHERE film_id = ?";
        super.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        updateFilmGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);

        String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(insertSql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String query = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        // ✅ ИСПРАВЛЕНО: Правильный маппинг для популярных фильмов
        String query = "SELECT f.*, r.name as mpa_name, COUNT(fl.user_id) as likes_count " +
                "FROM film f " +
                "JOIN rating_mpa r ON f.rating_id = r.rating_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration_in_minutes, f.rating_id, r.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbc.query(query, mapper, count);

        // ✅ Загружаем жанры для всех фильмов
        films.forEach(this::loadFilmGenres);

        return films;
    }

    private void validateMpaExists(Integer mpaId) {
        String query = "SELECT COUNT(*) FROM rating_mpa WHERE rating_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, mpaId);

        if (count == null || count == 0) {
            throw new NotFoundException(String.format("MPA рейтинг с id %d не найден.", mpaId));
        }
    }

    private void validateGenresExist(List<Genre> genres) {
        for (Genre genre : genres) {
            String query = "SELECT COUNT(*) FROM genre WHERE genre_id = ?";
            Integer count = jdbc.queryForObject(query, Integer.class, genre.getId());

            if (count == null || count == 0) {
                throw new NotFoundException(String.format("Жанр с id %d не найден.", genre.getId()));
            }
        }
    }

    private void saveFilmGenres(Long filmId, List<Genre> genres) {
        List<Genre> uniqueGenres = genres.stream()
                .distinct()
                .toList();

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setInt(2, uniqueGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return uniqueGenres.size();
            }
        });
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.genre_id";

        List<Genre> genres = jdbc.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        film.setGenres(genres);
    }

    private void updateFilmGenres(Long filmId, List<Genre> genres) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbc.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        }
    }
}