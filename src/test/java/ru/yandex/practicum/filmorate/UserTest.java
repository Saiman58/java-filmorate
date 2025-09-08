package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldNotCreateUserWithEmptyLogin() {
        User user = new User();
        user.setLogin(""); // Пустой??

        assertTrue(user.getLogin().isEmpty(), "Логин не может быть пустым");
    }

    @Test
    void shouldNotCreateUserWithLoginContainingSpaces() {
        User user = new User();
        user.setLogin("user name"); // пробел??

        assertTrue(user.getLogin().contains(" "), "Логин не может содержать пробелы");
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() {
        User user = new User();
        user.setLogin("username");
        user.setBirthday(LocalDate.now().plusDays(1)); //будущем??

        assertTrue(user.getBirthday().isAfter(LocalDate.now()), "Дата рождения не может быть в будущем");
    }

    @Test
    void shouldSetNameToLoginIfNameIsNull() {
        User user = new User();
        user.setLogin("username");
        user.setName(null); // нет имени

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        assertEquals("username", user.getName(), "Имя должно быть установлено в логин");
    }
}