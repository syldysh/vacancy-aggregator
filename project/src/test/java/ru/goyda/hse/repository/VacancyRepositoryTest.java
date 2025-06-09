package ru.goyda.hse.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.goyda.hse.model.Vacancy;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
class VacancyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VacancyRepository vacancyRepository;

    private Vacancy vacancy1;
    private Vacancy vacancy2;
    private Vacancy vacancy3;

    @BeforeEach
    void setUp() {
        vacancy1 = new Vacancy();
        vacancy1.setTitle("Java Developer");
        vacancy1.setCompany("Tech Company");
        vacancy1.setCity("Москва");
        vacancy1.setSalary("100000-150000");
        vacancy1.setDescription("Разработка Java приложений с использованием Spring Framework");
        vacancy1.setRequirements("Опыт работы с Java, Spring, SQL");
        vacancy1.setPublicationDate(LocalDateTime.now());
        vacancy1.setLink("https://example.com/vacancy/1");
        vacancy1.setSource("https://trudvsem.ru/");

        vacancy2 = new Vacancy();
        vacancy2.setTitle("Python Developer");
        vacancy2.setCompany("Another Company");
        vacancy2.setCity("Санкт-Петербург");
        vacancy2.setSalary("80000-120000");
        vacancy2.setDescription("Разработка Python приложений с Django");
        vacancy2.setRequirements("Опыт работы с Python, Django, PostgreSQL");
        vacancy2.setPublicationDate(LocalDateTime.now());
        vacancy2.setLink("https://example.com/vacancy/2");
        vacancy2.setSource("https://trudvsem.ru/");

        vacancy3 = new Vacancy();
        vacancy3.setTitle("Frontend Developer");
        vacancy3.setCompany("Tech Company");
        vacancy3.setCity("Новосибирск");
        vacancy3.setSalary("70000-100000");
        vacancy3.setDescription("Разработка пользовательских интерфейсов");
        vacancy3.setRequirements("Опыт работы с React, CSS, HTML");
        vacancy3.setPublicationDate(LocalDateTime.now());
        vacancy3.setLink("https://example.com/vacancy/3");
        vacancy3.setSource("https://hh.ru/");

        entityManager.persistAndFlush(vacancy1);
        entityManager.persistAndFlush(vacancy2);
        entityManager.persistAndFlush(vacancy3);
    }

    @Test
    void findByKeyword_ShouldFindVacanciesByTitleKeyword() {
         
        List<Vacancy> result = vacancyRepository.findByKeyword("Java");

           
        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).getTitle());
    }

    @Test
    void findByKeyword_ShouldFindVacanciesByDescriptionKeyword() {
         
        List<Vacancy> result = vacancyRepository.findByKeyword("Django");

          
        assertEquals(1, result.size());
        assertEquals("Python Developer", result.get(0).getTitle());
    }

    @Test
    void findByKeyword_ShouldFindVacanciesByRequirementsKeyword() {
         
        List<Vacancy> result = vacancyRepository.findByKeyword("React");

           
        assertEquals(1, result.size());
        assertEquals("Frontend Developer", result.get(0).getTitle());
    }

    @Test
    void findByKeyword_ShouldBeCaseInsensitive() {
         
        List<Vacancy> result = vacancyRepository.findByKeyword("java");

           
        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).getTitle());
    }

    @Test
    void findByFilters_WithCity_ShouldReturnVacanciesFromSpecificCity() {
         
        List<Vacancy> result = vacancyRepository.findByFilters("Москва", null, null, null, null);

           
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Москва");
    }

    @Test
    void findByFilters_WithCompany_ShouldReturnVacanciesFromSpecificCompany() {
         
        List<Vacancy> result = vacancyRepository.findByFilters(null, "Tech Company", null, null, null);

           
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(v -> v.getCompany().equals("Tech Company"));
    }

    @Test
    void findByFilters_WithKeyword_ShouldReturnVacanciesMatchingKeyword() {
         
        List<Vacancy> result = vacancyRepository.findByFilters(null, null, "Python", null, null);

           
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Python");
    }

    @Test
    void findByFilters_WithAllFilters_ShouldReturnMatchingVacancies() {
         
        List<Vacancy> result = vacancyRepository.findByFilters("Москва", "Tech Company", "Java", null, null);

           
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Developer");
    }

    @Test
    void findByFilters_WithNonExistentCity_ShouldReturnEmptyList() {
         
        List<Vacancy> result = vacancyRepository.findByFilters("Екатеринбург", null, null, null, null);

           
        assertThat(result).isEmpty();
    }

    @Test
    void findAllCities_ShouldReturnDistinctCitiesInOrder() {
         
        List<String> result = vacancyRepository.findAllCities();

           
        assertEquals(3, result.size());
        assertTrue(result.contains("Москва"));
        assertTrue(result.contains("Санкт-Петербург"));
        assertTrue(result.contains("Новосибирск"));
        // Проверяем, что результат отсортирован
        assertEquals("Москва", result.get(0));
        assertEquals("Новосибирск", result.get(1));
        assertEquals("Санкт-Петербург", result.get(2));
    }

    @Test
    void findAllCompanies_ShouldReturnDistinctCompaniesInOrder() {
         
        List<String> result = vacancyRepository.findAllCompanies();

           
        assertEquals(2, result.size());
        assertTrue(result.contains("Tech Company"));
        assertTrue(result.contains("Another Company"));
        // Проверяем, что результат отсортирован
        assertEquals("Another Company", result.get(0));
        assertEquals("Tech Company", result.get(1));
    }

    @Test
    void existsByLinkAndSource_WhenExists_ShouldReturnTrue() {
         
        boolean result = vacancyRepository.existsByLinkAndSource("https://example.com/vacancy/1", "https://trudvsem.ru/");

           
        assertTrue(result);
    }

    @Test
    void existsByLinkAndSource_WhenNotExists_ShouldReturnFalse() {
         
        boolean result = vacancyRepository.existsByLinkAndSource("https://example.com/vacancy/999", "https://trudvsem.ru/");

           
        assertFalse(result);
    }

    @Test
    void existsByLinkAndSource_WithDifferentSource_ShouldReturnFalse() {
         
        boolean result = vacancyRepository.existsByLinkAndSource("https://example.com/vacancy/1", "https://hh.ru/");

           
        assertFalse(result);
    }
} 