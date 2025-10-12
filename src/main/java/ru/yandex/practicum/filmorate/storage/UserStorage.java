package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

public interface UserStorage {
    public User createUser(User user);

    public User updateUser(User updatedUser);

    public User getUserById(Long userId);

    public ArrayList<User> getUsers();

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getAllFriends(Long userId);

    List<User> getMutualFriends(Long userId1, Long userId2);
}

