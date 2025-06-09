package ru.goyda.hse.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy")
@Data
@NoArgsConstructor
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Company cannot be blank")
    private String company;

    @NotBlank(message = "City cannot be blank")
    private String city;

    private String salary;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Requirements cannot be blank")
    private String requirements;

    @NotNull(message = "Publication date cannot be null")
    private LocalDateTime publicationDate;

    @NotBlank(message = "Link cannot be blank")
    @URL(message = "Link must be a valid URL")
    private String link;

    private String source = "https://trudvsem.ru/";
}
