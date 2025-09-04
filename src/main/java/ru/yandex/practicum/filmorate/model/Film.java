package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private Long id; //идентификатор
    private String name; //название
    private String description; //описание
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate; //дата релиза (под вопросом)
    private Double duration; //продолжительность фильма

}
