package com.example.livelib.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD}) // Аннотация применяется к полю
@Retention(RetentionPolicy.RUNTIME) // Аннотация доступна во время выполнения
@Constraint(validatedBy = UniqueIsbnValidator.class) // Указываем класс валидатора
public @interface UniqueIsbn {

    String message() default "ISBN must be unique"; // Сообщение по умолчанию

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}