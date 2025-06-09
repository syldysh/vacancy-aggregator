package ru.goyda.hse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.goyda.hse.model.Vacancy;
import ru.goyda.hse.service.TrudvsemParserService;
import ru.goyda.hse.service.VacancyService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VacancyController.class)
class VacancyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VacancyService vacancyService;

    @MockBean
    private TrudvsemParserService trudvsemParserService;

    @Autowired
    private ObjectMapper objectMapper;

    private Vacancy testVacancy;
    private List<Vacancy> testVacancies;

    @BeforeEach
    void setUp() {
        testVacancy = new Vacancy();
        testVacancy.setId(1L);
        testVacancy.setTitle("Java Developer");
        testVacancy.setCompany("Tech Company");
        testVacancy.setCity("Москва");
        testVacancy.setSalary("100000-150000");
        testVacancy.setDescription("Разработка Java приложений");
        testVacancy.setRequirements("Опыт работы с Java, Spring");
        testVacancy.setPublicationDate(LocalDateTime.now());
        testVacancy.setLink("https://example.com/vacancy/1");
        testVacancy.setSource("https://trudvsem.ru/");

        Vacancy vacancy2 = new Vacancy();
        vacancy2.setId(2L);
        vacancy2.setTitle("Python Developer");
        vacancy2.setCompany("Another Company");
        vacancy2.setCity("Санкт-Петербург");
        vacancy2.setSalary("80000-120000");
        vacancy2.setDescription("Разработка Python приложений");
        vacancy2.setRequirements("Опыт работы с Python, Django");
        vacancy2.setPublicationDate(LocalDateTime.now());
        vacancy2.setLink("https://example.com/vacancy/2");
        vacancy2.setSource("https://trudvsem.ru/");

        testVacancies = Arrays.asList(testVacancy, vacancy2);
    }

    @Test
    void getAllVacancies_ShouldReturnVacanciesList() throws Exception {
        when(vacancyService.getAllVacancies()).thenReturn(testVacancies);

        mockMvc.perform(get("/api/vacancies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Java Developer")))
                .andExpect(jsonPath("$[0].company", is("Tech Company")))
                .andExpect(jsonPath("$[1].title", is("Python Developer")))
                .andExpect(jsonPath("$[1].company", is("Another Company")));

        verify(vacancyService).getAllVacancies();
    }

    @Test
    void getVacancyById_WhenVacancyExists_ShouldReturnVacancy() throws Exception {
        when(vacancyService.getVacancyById(1L)).thenReturn(Optional.of(testVacancy));

        mockMvc.perform(get("/api/vacancies/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Java Developer")))
                .andExpect(jsonPath("$.company", is("Tech Company")))
                .andExpect(jsonPath("$.city", is("Москва")));

        verify(vacancyService).getVacancyById(1L);
    }

    @Test
    void getVacancyById_WhenVacancyNotExists_ShouldReturnNotFound() throws Exception {
        when(vacancyService.getVacancyById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/vacancies/999"))
                .andExpect(status().isNotFound());

        verify(vacancyService).getVacancyById(999L);
    }

    @Test
    void searchVacancies_WithAllParameters_ShouldReturnFilteredVacancies() throws Exception {
        when(vacancyService.getVacanciesByFilters("Москва", "Tech Company", "Java", null, null))
                .thenReturn(Arrays.asList(testVacancy));

        mockMvc.perform(get("/api/vacancies/search")
                        .param("city", "Москва")
                        .param("company", "Tech Company")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Java Developer")));

        verify(vacancyService).getVacanciesByFilters("Москва", "Tech Company", "Java", null, null);
    }

    @Test
    void searchVacancies_WithoutParameters_ShouldReturnAllVacancies() throws Exception {
        when(vacancyService.getVacanciesByFilters(null, null, null, null, null))
                .thenReturn(testVacancies);

        mockMvc.perform(get("/api/vacancies/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(vacancyService).getVacanciesByFilters(null, null, null, null, null);
    }

    @Test
    void searchVacancies_WithSalaryRange_ShouldReturnFilteredVacancies() throws Exception {
        when(vacancyService.getVacanciesByFilters(null, null, null, 50000, 150000))
                .thenReturn(Arrays.asList(testVacancy));

        mockMvc.perform(get("/api/vacancies/search")
                        .param("salaryFrom", "50000")
                        .param("salaryTo", "150000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(vacancyService).getVacanciesByFilters(null, null, null, 50000, 150000);
    }

    @Test
    void getAllCities_ShouldReturnCitiesList() throws Exception {
        List<String> cities = Arrays.asList("Москва", "Санкт-Петербург", "Новосибирск");
        when(vacancyService.getAllCities()).thenReturn(cities);

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("Москва", "Санкт-Петербург", "Новосибирск")));

        verify(vacancyService).getAllCities();
    }

    @Test
    void getAllCompanies_ShouldReturnCompaniesList() throws Exception {
        List<String> companies = Arrays.asList("Tech Company", "Another Company", "Big Corp");
        when(vacancyService.getAllCompanies()).thenReturn(companies);

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("Tech Company", "Another Company", "Big Corp")));

        verify(vacancyService).getAllCompanies();
    }


    @Test
    void parseVacancies_WithDefaultPages_ShouldReturnSuccess() throws Exception {
        doNothing().when(trudvsemParserService).parseAndSaveVacancies(1);

        mockMvc.perform(post("/api/parse-vacancies"))
                .andExpect(status().isOk())
                .andExpect(content().string("Parsing completed successfully for 1 page(s)"));

        verify(trudvsemParserService).parseAndSaveVacancies(1);
    }

    @Test
    void parseVacancies_WithCustomPages_ShouldReturnSuccess() throws Exception {
        doNothing().when(trudvsemParserService).parseAndSaveVacancies(5);

        mockMvc.perform(post("/api/parse-vacancies")
                        .param("pages", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Parsing completed successfully for 5 page(s)"));

        verify(trudvsemParserService).parseAndSaveVacancies(5);
    }

    @Test
    void parseVacancies_WithInvalidPages_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/parse-vacancies")
                        .param("pages", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pages parameter must be between 1 and 100"));

        verify(trudvsemParserService, never()).parseAndSaveVacancies(anyInt());
    }

    @Test
    void parseVacancies_WithZeroPages_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/parse-vacancies")
                        .param("pages", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pages parameter must be between 1 and 100"));

        verify(trudvsemParserService, never()).parseAndSaveVacancies(anyInt());
    }

    @Test
    void parseVacancies_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Parsing error")).when(trudvsemParserService).parseAndSaveVacancies(1);

        mockMvc.perform(post("/api/parse-vacancies"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error during parsing: Parsing error"));

        verify(trudvsemParserService).parseAndSaveVacancies(1);
    }
} 