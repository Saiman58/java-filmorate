package ru.yandex.practicum.filmorate.dal.storage.mpa;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.RatingMpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JdbcMpaRepository implements MpaRepository {
    RowMapper<RatingMpa> mapper = new RatingMpaRowMapper();
    JdbcTemplate jdbc;

    @Override
    public RatingMpa getMpaById(int mpaId) {
        Optional<RatingMpa> mpa = Optional.ofNullable(jdbc.queryForObject("SELECT * FROM rating_mpa " +
                "WHERE rating_id = ?", mapper, mpaId));
        if (mpa.isPresent()) {
            return mpa.get();
        } else {
            throw new NotFoundException("Рейтинг не найден");
        }
    }

    @Override
    public Collection<RatingMpa> getAllMpa() {
        log.debug("getAll().");
        List<RatingMpa> result = jdbc.query("SELECT * FROM rating_mpa ORDER BY rating_id", mapper);
        log.trace("Возвращены все рейтинги MPA: {}.", result);
        return result;
    }
}