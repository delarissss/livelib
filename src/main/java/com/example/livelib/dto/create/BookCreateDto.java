package com.example.livelib.dto.create;

import com.example.livelib.utils.validation.UniqueIsbn;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Setter
public class BookCreateDto {

    private String title;
    private String description;
    @UniqueIsbn
    private String isbn;
    private String publicationYear;
    @Setter
    @Getter
    private String authorId;
    @Getter
    @Setter
    private List<String> genreIds;

    @NotBlank(message = "Название книги обязательно")
    @Size(max = 500, message = "Название книги не может превышать 500 символов")
    public String getTitle(){
        return title;
    }

    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    public String getDescription() {
        return description;
    }

    @Size(max = 20, message = "ISBN не может превышать 20 символов")
    public String getIsbn() {
        return isbn;
    }

    @Positive(message = "Год публикации должен быть положительным числом")
    @PastOrPresent(message = "Год публикации не должен быть в будущем!")
    public String getPublicationYear() {
        return publicationYear;
    }

}
