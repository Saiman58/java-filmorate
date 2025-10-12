package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id; //уникальный идентификатор пользователя

    @NotBlank(message = "Электронная почта должна быть обязательно заполнена")
    @Email(message = "Некорректный Email")   // Проверка на корректность электронной почты
    private String email; //электронная почта пользователя

    @NotBlank(message = "Логин должен быть обязательно заполнен") // Проверка на пустой логин и наличие пробелов
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелов") // не должен содержать пробелов
    private String login; //логин пользователя

    private String name; //имя для отображения

    @NotNull(message = "Дата рождения не может быть пустой")
    @JsonFormat(pattern = "yyyy-MM-dd") //формат даты
    @Past(message = "Дата рождения не может быть в будущем")  // Проверка на дату рождения
    private LocalDate birthday; //дата рождения

    private Set<Long> friends = new HashSet<>(); //идентификаторов друзей

    // НОВОЕ ПОЛЕ: ID подтвержденных друзей
    private Set<Long> confirmedFriends = new HashSet<>();
}

/*
*
@NotBlank поле не null и не пустое
@Email поле не null и валидный email
@Past поле не null и дата в прошлом
@NonNull поле не null
*
* */
