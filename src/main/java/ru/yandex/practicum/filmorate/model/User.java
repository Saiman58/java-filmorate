package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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
    @NonNull
    @NotBlank
    private String email; //электронная почта пользователя
    private String login; //логин пользователя
    private String name; //имя для отображения
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday; //дата рождения
    private Set<Long> friends = new HashSet<>(); //идентификаторов друзей
}
