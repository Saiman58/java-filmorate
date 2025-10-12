package ru.yandex.practicum.filmorate.dal.storage.film;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.LikeUserIdsRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.dal.storage.genre.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.dal.storage.mpa.JdbcMpaRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository("jdbcFilmRepository")
@Primary
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JdbcFilmRepository extends BaseRepository<Film> implements FilmRepository, FilmStorage {
    RowMapper<Film> mapper = new FilmRowMapper();
    JdbcMpaRepository jdbcMpaRepository;
    UserStorage userStorage;
    JdbcGenreRepository jdbcGenreRepository;
    private JdbcOperations jdbcTemplate;

    public JdbcFilmRepository(JdbcTemplate jdbc, JdbcMpaRepository jdbcMpaRepository,
                              UserStorage userStorage, JdbcGenreRepository jdbcGenreRepository, JdbcOperations jdbcTemplate) {
        super(jdbc);
        this.jdbcMpaRepository = jdbcMpaRepository;
        this.userStorage = userStorage;
        this.jdbcGenreRepository = jdbcGenreRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        RatingMpa mpa = jdbcMpaRepository.getMpaById(film.getMpa().getId());

        String query = "INSERT INTO film(name, description, release_date, " +
                "duration_in_minutes, rating_id) VALUES (?, ?, ?, ?, ?)";
        long id = super.create(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        film.setLikes(new HashSet<>());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenresToDB(film.getId(), film.getGenres());
        }
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            for (Long userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }

        film.setMpa(mpa);

        return film;
    }

    @Override
    public Film getById(Long id) {
        String query = "SELECT * FROM film WHERE film_id = ?";
        Film film = findOne(query, mapper, id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден.", id)));
        Set<Long> likes = getLikeUserIdsFromDB(id);
        film.setLikes(likes);
        List<Genre> genres = getGenreFromDB(id);
        film.setGenres(genres);
        film.setMpa(jdbcMpaRepository.getMpaById(film.getMpa().getId()));
        return film;
    }

    @Override
    public Map<Long, Film> getAll() {
        return getAllValues().stream()
                .collect(Collectors.toMap(Film::getId, film -> film));
    }

    @Override
    public List<Film> getAllValues() {
        String query = "SELECT f.*, r.rating_id as mpa_id, r.name as mpa_name " +
                "FROM film f " +
                "JOIN rating_mpa r ON f.rating_id = r.rating_id";
        List<Film> films = findMany(query, mapper);
        for (Film film : films) {
            film.setLikes(getLikeUserIdsFromDB(film.getId()));
            film.setGenres(getGenreFromDB(film.getId()));
        }
        return films;
    }

    @Override
    public Film update(Film film) {
        RatingMpa mpa = jdbcMpaRepository.getMpaById(film.getMpa().getId());
        String query = "UPDATE film SET name = ?, description = ?, release_date = ?, duration_in_minutes = ?, " +
                "rating_id = ? WHERE film_id = ?";
        super.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpa.getId(),
                film.getId()
        );

        deleteFilmGenresFromDB(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addFilmGenresToDB(film.getId(), film.getGenres());
        }
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            for (Long userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }
        return film;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM film WHERE film_id = ?";
        boolean isDeleted = super.delete(query, id);

        if (!isDeleted) {
            throw new InternalServerException(String.format("Не удалось удалить фильм с id: %d", id));
        }

        deleteFilmGenresFromDB(id);
    }

    @Override
    public Set<Long> getLikeUserIdsFromDB(long id) {
        String query = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbc.query(query, new LikeUserIdsRowMapper(), id));
    }

    @Override
    public List<Genre> getGenreFromDB(long id) {
        String query = "SELECT g.genre_id, g.name FROM film_genre fg " +
                "INNER JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return new ArrayList<>(jdbc.query(query, new GenreRowMapper(), id));
    }

    public boolean deleteFilmGenresFromDB(Long filmId) {
        String query = "DELETE FROM film_genre WHERE film_id = ?";
        int rowsDeleted = jdbc.update(query, filmId);
        return rowsDeleted > 0;
    }

    public boolean addFilmGenresToDB(Long filmId, List<Genre> genres) {
        for (Genre genre : genres) {
            jdbcGenreRepository.getGenreById(genre.getId());
        }

        String query = "MERGE INTO film_genre AS target " +
                "KEY (film_id, genre_id) " +
                "VALUES (?, ?)";

        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Genre genre = genres.get(i);  // ← Берем жанр по индексу
                preparedStatement.setLong(1, filmId);
                preparedStatement.setLong(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();  // ← Возвращаем реальный размер
            }
        });

        return true;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String query = "SELECT f.*, COUNT(fu.user_id) as likes_count " +
                "FROM film f " +
                "LEFT JOIN film_likes fu ON f.film_id = fu.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbc.query(query, mapper, count);

        for (Film film : films) {
            film.setLikes(getLikeUserIdsFromDB(film.getId()));
            film.setGenres(getGenreFromDB(film.getId()));
            film.setMpa(jdbcMpaRepository.getMpaById(film.getMpa().getId()));
        }

        return films;
    }

    @Override
    public int getLikeCount(Film film) {
        return film.getLikes().size();
    }

    @Override
    public void addLike(Long filmId, Long userId) {

        userStorage.getUserById(userId);
        getById(filmId);


        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            return;
        }


        String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

        try {
            jdbc.update(insertSql, filmId, userId);
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public Film createFilm(Film film) {
        return create(film);
    }

    @Override
    public Film updateFilm(Film film) {
        return update(film);
    }

    @Override
    public Film getFilmById(Long filmId) {
        return getById(filmId);
    }

    @Override
    public List<Film> getFilms() {
        return getAllValues();
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        getById(filmId);
        userStorage.getUserById(userId);

        String query = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);
    }
}