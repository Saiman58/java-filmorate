package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id; //уникальный идентификатор пользователя
    @NonNull
    @NotBlank
    @Email
    private String email; //электронная почта пользователя
    private String login; //логин пользователя
    private String name; //имя для отображения
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday; //дата рождения
}
