package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter
public class BookCreateDto {

    @NotBlank(message = "Название книги обязательно")
    @Size(max = 500, message = "Название книги не может превышать 500 символов")
    private String title;

    @NotNull(message = "ID автора обязателен")
    private Long authorId;

    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    private String description;

    @Size(max = 20, message = "ISBN не может превышать 20 символов")
    private String isbn;

    @Positive(message = "Год публикации должен быть положительным числом")
    private Integer publicationYear;

    private List<Long> genreIds;

}
