package ru.yandex.practicum.filmorate.dal.storage.mpa;

import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.Collection;

public interface MpaRepository {
    RatingMpa getMpaById(int mpaId);

    Collection<RatingMpa> getAllMpa();
}