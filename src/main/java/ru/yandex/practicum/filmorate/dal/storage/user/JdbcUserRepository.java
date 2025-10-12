package ru.yandex.practicum.filmorate.dal.storage.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FriendIdsRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.storage.BaseRepository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository("jdbcUserRepository")
@Primary
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JdbcUserRepository extends BaseRepository<User>
        implements UserRepository, UserStorage {
    RowMapper<User> mapper = new UserRowMapper();

    public JdbcUserRepository(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public User create(User user) {
        String query = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
        long id = super.create(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );

        user.setId(id);
        // Сохраняем существующих друзей или иниц. пустой set
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        return user;
    }

    public User getById(Long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        User user = findOne(query, mapper, id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден.", id)));

        Set<Long> friends = getFriendIdsFromDB(id);
        user.setFriends(friends != null ? friends : new HashSet<>());

        return user;
    }

    @Override
    public Map<Long, User> getAll() {
        return getAllValues().stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    @Override
    public List<User> getAllValues() {
        String query = "SELECT * FROM users";
        List<User> users = findMany(query, mapper);
        for (User user : users) {
            user.setFriends(getFriendIdsFromDB(user.getId()));
        }
        return users;
    }

    @Override
    public User update(User user) {
        String query = "UPDATE users SET email = ?, login = ?, name = ?,  birthday = ?  WHERE user_id = ?";
        super.update(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        Set<Long> currentFriends = getFriendIdsFromDB(user.getId());
        user.setFriends(currentFriends);

        return user;
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM users WHERE user_id = ?";
        boolean isDeleted = super.delete(query, id);

        if (!isDeleted) {
            throw new InternalServerException(String.format("Не удалось удалить пользователя с id: %d.", id));
        }
    }

    public User findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        User user = findOne(query, mapper, email)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с email %s не найден.", email)));
        Set<Long> friends = getFriendIdsFromDB(user.getId());
        user.setFriends(friends);
        return user;
    }


    @Override
    public void addFriend(Long userId, Long friendId) {
        String userExistsQuery = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer userCount = jdbc.queryForObject(userExistsQuery, Integer.class, userId);
        Integer friendCount = jdbc.queryForObject(userExistsQuery, Integer.class, friendId);

        if (userCount == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (friendCount == 0) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья");
        }

        String checkQuery = "SELECT COUNT(*) FROM friendship_status WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(checkQuery, Integer.class, userId, friendId);

        if (count == 0) {
            String query = "INSERT INTO friendship_status (user_id, friend_id) VALUES (?, ?)";
            jdbc.update(query, userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);

        String query = "DELETE FROM friendship_status WHERE user_id = ? AND friend_id = ?";
        jdbc.update(query, userId, friendId);

    }

    @Override
    public Set<Long> getFriendIdsFromDB(long id) {
        String query = "SELECT friend_id FROM friendship_status WHERE user_id = ?";
        return new HashSet<>(jdbc.query(query, new FriendIdsRowMapper(), id));
    }

    @Override
    public List<User> getAllFriends(Long userId) {
        getById(userId); // Проверяем существование
        String query = "SELECT u.* FROM users u " +
                "INNER JOIN friendship_status f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.user_id";
        return findMany(query, mapper, userId);
    }


    @Override
    public List<User> getMutualFriends(Long userId1, Long userId2) {
        getById(userId1);
        getById(userId2);

        String query = "SELECT u.* FROM users u " +
                "INNER JOIN friendship_status f1 ON u.user_id = f1.friend_id AND f1.user_id = ? " +
                "INNER JOIN friendship_status f2 ON u.user_id = f2.friend_id AND f2.user_id = ? " +
                "ORDER BY u.user_id";

        return findMany(query, mapper, userId1, userId2);
    }

    @Override
    public User createUser(User user) {
        return create(user);
    }

    @Override
    public User updateUser(User updatedUser) {
        return update(updatedUser);
    }

    @Override
    public User getUserById(Long userId) {
        return getById(userId);
    }

    @Override
    public ArrayList<User> getUsers() {
        return new ArrayList<>(getAllValues());
    }
}