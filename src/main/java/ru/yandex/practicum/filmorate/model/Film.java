package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id; //идентификатор

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name; //название

    @NotBlank(message = "Описание фильма не может быть пустым")
    @Size(max = 200, message = "Максимальная количество символов 200")
    private String description; //описание


    @NotNull(message = "Дата релиза не может быть пустой")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate; //дата релиза (под вопросом)


    @Min(value = 1, message = "Продолжительность не может быть равна или меньше 0")
    private Long duration; //продолжительность фильма

    @Builder.Default
    Set<Long> likes = new HashSet<>();

    @NotNull(message = "У фильма должен быть указан рейтинг MPA")
    RatingMpa mpa;

    @Builder.Default
    List<Genre> genres = new ArrayList<>();

}

/*
*
@NotBlank поле не null и не пустое
@Email поле не null и валидный email
@Past поле не null и дата в прошлом
@NonNull поле не null
*
* */
