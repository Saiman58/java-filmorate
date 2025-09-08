package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    //вывод всех пользователей
    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    //Добавить пользователя
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        try {
            // Проверка на пустой логин и наличие пробелов
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                log.error("Логин не может быть пустым и содержать пробелы");
                throw new ValidationException(("Логин не может быть пустым и содержать пробелы"));
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

    //обновить информацию и пользователе
    @PutMapping
    public User update(@RequestBody User updateUser) {
        // Проверка на null для ID пользователя
        if (updateUser.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        // Проверка на существование пользователя
        if (!users.containsKey(updateUser.getId())) {
            log.error("Пользователь с id = {} не найден", updateUser.getId());
            throw new ValidationException("Пользователь с id = " + updateUser.getId() + " не найден");
        }

        User existingUser = users.get(updateUser.getId());

        // Обновление email, если он указан и корректен
        if (updateUser.getEmail() != null && updateUser.getEmail().contains("@")) {
            existingUser.setEmail(updateUser.getEmail());
            log.info("Email обновлен");
        }

        // Обновление логина, если он указан и корректен
        if (updateUser.getLogin() != null && !updateUser.getLogin().isBlank() && !updateUser.getLogin().contains(" ")) {
            existingUser.setLogin(updateUser.getLogin());
            log.info("Логин обновлен");
        }

        // Обновление имени, если оно указано
        if (updateUser.getName() != null) {
            existingUser.setName(updateUser.getName());
            log.info("Имя обновлено");
        }

        LocalDate currentDate = LocalDate.now(); //тек. дата
        // Обновление даты рождения, если она указана и корректна
        if (updateUser.getBirthday() != null && !updateUser.getBirthday().isAfter(currentDate)) {
            existingUser.setBirthday(updateUser.getBirthday());
            log.info("Дата рождения обновлена");
        }
        return existingUser;
    }
}
