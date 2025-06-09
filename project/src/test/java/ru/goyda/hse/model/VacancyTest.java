package ru.goyda.hse.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VacancyTest {

    private Validator validator;
    private Vacancy vacancy;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        vacancy = new Vacancy();
        vacancy.setTitle("Java Developer");
        vacancy.setCompany("Tech Company");
        vacancy.setCity("Москва");
        vacancy.setSalary("100000-150000");
        vacancy.setDescription("Разработка Java приложений");
        vacancy.setRequirements("Опыт работы с Java, Spring");
        vacancy.setPublicationDate(LocalDateTime.now());
        vacancy.setLink("https://example.com/vacancy/1");
        vacancy.setSource("https://trudvsem.ru/");
    }

    @Test
    void validVacancy_ShouldPassValidation() {

        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertTrue(violations.isEmpty());
    }

    @Test
    void vacancyWithNullTitle_ShouldFailValidation() {

        vacancy.setTitle(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void vacancyWithBlankTitle_ShouldFailValidation() {

        vacancy.setTitle("   ");


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void vacancyWithNullCompany_ShouldFailValidation() {

        vacancy.setCompany(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("company")));
    }

    @Test
    void vacancyWithNullCity_ShouldFailValidation() {

        vacancy.setCity(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("city")));
    }

    @Test
    void vacancyWithNullDescription_ShouldFailValidation() {

        vacancy.setDescription(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void vacancyWithNullRequirements_ShouldFailValidation() {

        vacancy.setRequirements(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("requirements")));
    }

    @Test
    void vacancyWithNullPublicationDate_ShouldFailValidation() {

        vacancy.setPublicationDate(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("publicationDate")));
    }

    @Test
    void vacancyWithNullLink_ShouldFailValidation() {

        vacancy.setLink(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("link")));
    }

    @Test
    void vacancyWithNullSalary_ShouldPassValidation() {

        vacancy.setSalary(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertTrue(violations.isEmpty()); // Salary is optional
    }

    @Test
    void vacancyWithNullSource_ShouldPassValidation() {

        vacancy.setSource(null);


        Set<ConstraintViolation<Vacancy>> violations = validator.validate(vacancy);


        assertTrue(violations.isEmpty()); // Source is optional
    }

    @Test
    void vacancyEquality_ShouldWorkCorrectly() {

        Vacancy vacancy1 = new Vacancy();
        vacancy1.setId(1L);
        vacancy1.setTitle("Java Developer");

        Vacancy vacancy2 = new Vacancy();
        vacancy2.setId(1L);
        vacancy2.setTitle("Java Developer");

        Vacancy vacancy3 = new Vacancy();
        vacancy3.setId(2L);
        vacancy3.setTitle("Python Developer");


        assertEquals(vacancy1, vacancy2);
        assertNotEquals(vacancy1, vacancy3);
        assertEquals(vacancy1.hashCode(), vacancy2.hashCode());
    }

    @Test
    void vacancyToString_ShouldContainMainFields() {

        String result = vacancy.toString();


        assertTrue(result.contains("Java Developer"));
        assertTrue(result.contains("Tech Company"));
        assertTrue(result.contains("Москва"));
    }
} 