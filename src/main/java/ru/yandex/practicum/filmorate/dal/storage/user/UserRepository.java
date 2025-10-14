package ru.yandex.practicum.filmorate.dal.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserRepository extends UserStorage {

    User create(User user);

    User getById(Long id);

    Map<Long, User> getAll();

    List<User> getAllValues();

    User update(User user);

    void deleteById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriendIdsFromDB(long id);

    List<User> getMutualFriends(Long userId1, Long userId2);

    List<User> getAllFriends(Long userId);

    User findByEmail(String email);

}