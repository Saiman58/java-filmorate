package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * User.
 */
@Data
public class User {
    private Long id; //уникальный идентификатор пользователя
    private String email; //электронная почта пользователя
    private String login; //логин пользователя
    private String name; //имя для отображения
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday; //дата рождения
}
