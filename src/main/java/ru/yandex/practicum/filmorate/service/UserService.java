package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    //существует ли пользователь с таким id
    //если да, добавляем в друзья
    public void addFriend(Long userId, Long friendId) {
        log.info("Пользователь {} пытается добавить в друзья пользователя {}", userId, friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} уже является другом пользователя {}", userId, friendId);
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    //удалить из друзей
    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : commonFriendIds) {
            commonFriends.add(getUserById(friendId));
        }

        log.info("Общие друзья между пользователями {} и {}: {}", userId, otherUserId, commonFriends);
        return commonFriends;
    }

    // вспомогательный метод для поиска пользователя по id
    private User getUserById(Long userId) {
        User user = userStorage.findUserById(userId);
        if (user == null) {
            log.error("Пользователь не найден: userId= {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return user;
    }

    //получить список друзей
    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        List<User> friendsList = new ArrayList<>();

        for (Long friendId : user.getFriends()) {
            User friend = getUserById(friendId);
            friendsList.add(friend);
        }
        return friendsList;
    }
}
