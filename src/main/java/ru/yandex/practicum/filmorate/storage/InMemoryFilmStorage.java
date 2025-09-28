package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);

    /*
     *
     * Вывод всех фильмов
     *
     */
    @Override
    public Collection<Film> findAllFilms() {
        log.info("Запрос на вывод всех фильмов");
        return films.values();
    }

    /*
     *
     * Добавить фильм
     *
     */
    @Override
    public Film createFilm(Film film) {
        try {
            log.info("Попытка добавления фильма : {}", film);
            //Проверка на пустое описание

            if (film.getName() == null || film.getName().isBlank()) {
                log.error("Название фильма не может быть пустым");
                throw new ValidationException("Название фильма не может быть пустым");
            }

            if (film.getDescription().isBlank()) {
                log.error("Описание фильма не может быть пустыми");
                throw new ValidationException("Описание фильма не может быть пустыми");
            }

            // Проверка на существование фильма с таким названием
            for (Film existingFilm : films.values()) {
                if (existingFilm.getName().equals(film.getName())) {
                    log.error("Фильм с названием {} уже существует", film.getName());
                    throw new ValidationException("Фильм с таким названием уже существует");
                }
            }

            if (film.getDescription() == null || film.getDescription().length() > 200) {
                log.error("Максимальная количество символов 200");
                throw new ValidationException("Максимальная количество символов: 200");
            }

            //Проверка даты релиза
            LocalDate startingDate = LocalDate.of(1895, 12, 28); //рождение кино
            LocalDate currentDate = LocalDate.now(); //тек. дата
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(startingDate) ||
                    film.getReleaseDate().isAfter(currentDate)) {
                log.error("Дата релиза фильма не может быть раньше дня рождения кино");
                throw new ValidationException("Дата релиза фильма не может быть раньше дня рождения кино");
            }
            //Проверка продолжительности фильма
            if (film.getDuration() <= 0) {
                log.error("Продолжительность не может быть равна или меньше 0");
                throw new ValidationException("Продолжительность не может быть равна или меньше 0");
            }

            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Фильм успешно создан: {}", film);
            return film;
        } catch (ValidationException e) {
            log.error("Создание фильма завершилось ошибкой", e);
            throw e;
        } catch (Exception e) {
            log.error("Произошла непредвиденная ошибка", e);
            throw new RuntimeException("Произошла непредвиденная ошибка", e);
        }
    }

    /*
     *
     * Обновить фильм
     *
     */
    @Override
    public Film update(Film updatedFilm) {
        log.info("Попытка обновления фильма : {}", updatedFilm);
        //Проверка, что ID фильма указан
        if (updatedFilm.getId() == null) {
            log.error("Id должен быть указан");
            throw new NotFoundException("Id должен быть указан");
        }

        //Проверка, существует ли фильм с указанным ID
        if (!films.containsKey(updatedFilm.getId())) {
            log.error("Фильм с id = {} не найден", updatedFilm.getId());
            throw new NotFoundException("Фильм с id = " + updatedFilm.getId() + " не найден");
        }

        Film existingFilm = films.get(updatedFilm.getId());

        // Обновление названия фильма
        if (updatedFilm.getName() != null || updatedFilm.getName().isBlank() ||
                existingFilm.getName().equals(updatedFilm.getName())) {
            existingFilm.setName(updatedFilm.getName());
            log.info("Название фильма обновлено");
        }

        //Обновление описания фильма
        if (updatedFilm.getDescription() != null && updatedFilm.getDescription().length() <= 200 ||
                existingFilm.getDescription().equals(updatedFilm.getDescription())) {

            existingFilm.setDescription(updatedFilm.getDescription());
            log.info("Описание фильма обновлено");
        }

        //Обновление даты релиза фильма
        LocalDate startingDate = LocalDate.of(1895, 12, 28); //рождение кино
        LocalDate currentDate = LocalDate.now(); //тек. дата
        LocalDate releaseDate = updatedFilm.getReleaseDate();
        if (releaseDate != null) {
            if (!(releaseDate.isBefore(startingDate) || releaseDate.isAfter(currentDate))) {
                existingFilm.setReleaseDate(releaseDate);
                log.info("Дата релиза фильма обновлена");
            } else {
                log.error("Дата релиза фильма должна быть между {} и {}", startingDate, currentDate);
            }
        }

        // Обновление продолжительности фильма
        if (updatedFilm.getDuration() != null && updatedFilm.getDuration() > 0) {
            existingFilm.setDuration(updatedFilm.getDuration());
            log.info("Продолжительность фильма обновлено");
        }
        return existingFilm;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    /*
     *
     * Поиск фильма по id
     *
     */
    @Override
    public Film findFilmById(long filmId) {
        return films.get(filmId);
    }
}
