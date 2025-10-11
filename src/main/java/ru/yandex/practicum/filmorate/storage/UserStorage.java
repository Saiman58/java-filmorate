package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    //   получения всех пользователей
    Collection<User> findUserAll();

    //  создания нового пользователя
    User createUser(User user);

    //  обновления информации о существующем пользователе
    User update(User updateUser);

    //  поиск пользователя по его идентификатору
    User findUserById(long userId);


}
