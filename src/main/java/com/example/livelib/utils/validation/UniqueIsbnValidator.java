package com.example.livelib.utils.validation;


import com.example.livelib.repos.BookRepository; // Убедитесь, что импортируете правильный репозиторий
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueIsbnValidator implements ConstraintValidator<UniqueIsbn, String> {

    private BookRepository bookRepository;

    public void initialize(UniqueIsbn constraintAnnotation) {
    }

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.isBlank()) {
            return true;
        }
        return !bookRepository.existsByIsbn(isbn);
    }
}