package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id; //идентификатор
    private String name; //название
    private String description; //описание
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate; //дата релиза (под вопросом)
    private Double duration; //продолжительность фильма
}
