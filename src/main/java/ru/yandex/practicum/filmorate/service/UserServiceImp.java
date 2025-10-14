package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor

public class UserServiceImp implements UserService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    @Override
    public User createUser(User user) {
        userValidator.validate(user);
        uniqueEmail(user.getEmail());


        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }


        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User updatedUser) {
        userValidator.validate(updatedUser);
        if (updatedUser.getId() <= 0) {
            throw new ValidationException("ID пользователя обязателен для обновления");
        }
        getUserById(updatedUser.getId());
        return userStorage.updateUser(updatedUser);
    }


    @Override
    public User getUserById(Long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return user;
    }


    @Override
    public ArrayList<User> getUsers() {
        return userStorage.getUsers();
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return userStorage.getAllFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        return userStorage.getMutualFriends(userId1, userId2);
    }

    private void uniqueEmail(String email) {
        for (User savedUser : userStorage.getUsers()) {
            if (savedUser.getEmail().equalsIgnoreCase(email)) {
                throw new ValidationException("Этот email уже используется");
            }
        }
    }
}
