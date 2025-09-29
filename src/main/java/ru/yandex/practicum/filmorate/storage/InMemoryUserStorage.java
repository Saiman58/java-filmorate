package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Long, User> users = new HashMap<>();

    /*
     *
     * Вывод всех пользователей
     *
     */
    public Collection<User> findUserAll() {
        return users.values();
    }

    /*
     *
     * Добавить пользователя
     *
     */
    public User createUser(@Valid User user) {
        log.info("Попытка создания пользователя: {}", user);
        try {

            // Проверка на корректность электронной почты
            if (!user.getEmail().contains("@")) {
                log.error("Некорректный Email");
                throw new ValidationException("Некорректный Email");
            }

            // Проверка на пустой логин и наличие пробелов
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                log.error("Логин не может быть пустым и содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }

            // Проверка на существование пользователя с таким email
            for (User existingUser : users.values()) {
                if (existingUser.getEmail().equals(user.getEmail())) {
                    log.error("Пользователь с email {} уже существует", user.getEmail());
                    throw new ValidationException("Пользователь с таким email уже существует");
                }
            }

            // Установить имя в логин, если имя не указано
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            // Проверка на дату рождения
            LocalDate currentDate = LocalDate.now(); //тек. дата
            if (user.getBirthday() == null || user.getBirthday().isAfter(currentDate)) {
                log.error("Дата рождения не может быть в будущем или не заполнена");
                throw new ValidationException("Дата рождения не может быть в будущем или не заполнена");
            }

            user.setId(getNextId());
            users.put(user.getId(), user);
            log.info("Пользователь успешно создан: {}", user);
            return user;

        } catch (ValidationException e) {
            log.error("Создание пользователя завершилось ошибкой: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Произошла непредвиденная ошибка", e);
            throw new RuntimeException("Произошла непредвиденная ошибка", e);
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    /*
     *
     * обновить информацию и пользователе
     *
     */
    @Override
    public User update(@Valid User updateUser) {
        // Проверка на null для ID пользователя
        log.info("Попытка обновления данных пользователя: {}", updateUser);
        if (updateUser.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        // Проверка на существование пользователя
        findUserById(updateUser.getId());

        User existingUser = users.get(updateUser.getId());
        boolean sensorUpdate = false;

        // Обновление email, если он указан и корректен
        if (updateUser.getEmail() != null && updateUser.getEmail().contains("@") &&
                !updateUser.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(updateUser.getEmail());
            log.info("Email обновлен");
            sensorUpdate = true;
        }

        // Обновление логина, если он указан и корректен
        if (updateUser.getLogin() != null && !updateUser.getLogin().isBlank() && !updateUser.getLogin().contains(" ") &&
                !updateUser.getLogin().equals(existingUser.getLogin())) {
            existingUser.setLogin(updateUser.getLogin());
            log.info("Логин обновлен");
            sensorUpdate = true;
        }

        // Обновление имени, если оно указано
        if (updateUser.getName() != null && !updateUser.getName().equals(existingUser.getName())) {
            existingUser.setName(updateUser.getName());
            log.info("Имя обновлено");
            sensorUpdate = true;
        }

        LocalDate currentDate = LocalDate.now(); //тек. дата
        // Обновление даты рождения, если она указана и корректна
        if (updateUser.getBirthday() != null && !updateUser.getBirthday().isAfter(currentDate) &&
                !updateUser.getBirthday().equals(existingUser.getBirthday())) {
            existingUser.setBirthday(updateUser.getBirthday());
            log.info("Дата рождения обновлена");
            sensorUpdate = true;
        }

        // Обновление списка друзей
        if (updateUser.getFriends() != null) {
            // Обновляем список друзей, если он передан
            existingUser.setFriends(updateUser.getFriends());
            log.info("Список друзей обновлен: {}", existingUser.getFriends());
            sensorUpdate = true;
        }

        if (sensorUpdate) {
            users.put(existingUser.getId(), existingUser);
        } else {
            log.info("Данные пользователя с id={} не изменились", existingUser.getId());
        }

        return existingUser;
    }

    /*
     *
     * Поиск пользователя по id
     *
     */
    @Override
    public User findUserById(long userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return users.get(userId);
    }
}
