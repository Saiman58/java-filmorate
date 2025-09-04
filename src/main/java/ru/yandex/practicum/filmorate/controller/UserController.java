package ru.yandex.practicum.filmorate.controller;


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
    private final Map<Long, User> users = new HashMap<>();

    //вывод всех пользователей
    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    //Добавить пользователя
    @PostMapping
    public User create(@RequestBody User user) {

        if (user.getEmail() == null) {
            throw new ValidationException("Email должен быть заполнен");
        }
        char symbol = '@';
        if (!user.getEmail().contains(String.valueOf(symbol))) {
            throw new ValidationException(("Email заполнен некорректно"));
        }


        if (user.getLogin() == null || user.getLogin().isBlank() && user.getLogin().contains(" ")) {
            throw new ValidationException(("Логин не может быть пустым и содержать пробелы"));
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        LocalDate currentDate = LocalDate.now(); //тек. дата
        if (user.getBirthday().isAfter(currentDate)) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
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
        //поиск по id
        if (updateUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(updateUser.getId())) {
            throw new ValidationException("Пользователь с id = " + updateUser.getId() + " не найден");
        }

        User existingUser = users.get(updateUser.getId());

        char symbol = '@';
        if (updateUser.getEmail() != null && updateUser.getEmail().contains(String.valueOf(symbol))) {
            existingUser.setEmail(updateUser.getEmail());
        }

        if (updateUser.getLogin() != null && !updateUser.getLogin().isBlank() && !updateUser.getLogin().contains(" ")) {
            existingUser.setLogin(updateUser.getLogin());
        }

        if (updateUser.getName() != null) {
            existingUser.setName(updateUser.getName());
        }

        LocalDate currentDate = LocalDate.now(); //тек. дата
        if (updateUser.getBirthday() != null && !updateUser.getBirthday().isAfter(currentDate)) {
            existingUser.setBirthday(updateUser.getBirthday());
        }
        return existingUser;
    }
}
