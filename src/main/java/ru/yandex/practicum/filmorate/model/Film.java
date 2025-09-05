package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id; //идентификатор
    @NonNull
    @NotBlank
    private String name; //название
    private String description; //описание
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate; //дата релиза (под вопросом)
    private Double duration; //продолжительность фильма

}
