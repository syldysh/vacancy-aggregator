package ru.goyda.hse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.goyda.hse.model.Vacancy;
import ru.goyda.hse.repository.VacancyRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @InjectMocks
    private VacancyService vacancyService;

    private Vacancy testVacancy;
    private Vacancy vacancy2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testVacancy = new Vacancy();
        testVacancy.setId(1L);
        testVacancy.setTitle("Java Developer");
        testVacancy.setCompany("Test Company");
        testVacancy.setCity("Moscow");
        testVacancy.setSalary("100000");
        testVacancy.setDescription("Test Description");
        testVacancy.setRequirements("Test Requirements");
        testVacancy.setPublicationDate(LocalDateTime.now());
        testVacancy.setLink("https://example.com/vacancy/1");
        testVacancy.setSource("https://trudvsem.ru/");

        vacancy2 = new Vacancy();
        vacancy2.setId(2L);
        vacancy2.setTitle("Python Developer");
        vacancy2.setCompany("Another Company");
        vacancy2.setCity("Saint Petersburg");
        vacancy2.setSalary("120000");
        vacancy2.setDescription("Another Description");
        vacancy2.setRequirements("Another Requirements");
        vacancy2.setPublicationDate(LocalDateTime.now());
        vacancy2.setLink("https://example.com/vacancy/2");
        vacancy2.setSource("https://trudvsem.ru/");
    }

    @Test
    void getAllVacancies_ShouldReturnAllVacancies() {
        when(vacancyRepository.findAll()).thenReturn(Arrays.asList(testVacancy, vacancy2));

        List<Vacancy> result = vacancyService.getAllVacancies();

        assertEquals(2, result.size());
        verify(vacancyRepository, times(1)).findAll();
    }

    @Test
    void getVacancyById_WhenVacancyExists_ShouldReturnVacancy() {
        when(vacancyRepository.findById(1L)).thenReturn(Optional.of(testVacancy));

        Optional<Vacancy> result = vacancyService.getVacancyById(1L);

        assertTrue(result.isPresent());
        assertEquals(testVacancy, result.get());
        verify(vacancyRepository, times(1)).findById(1L);
    }

    @Test
    void getVacancyById_WhenVacancyDoesNotExist_ShouldReturnEmpty() {
        when(vacancyRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Vacancy> result = vacancyService.getVacancyById(999L);

        assertFalse(result.isPresent());
        verify(vacancyRepository, times(1)).findById(999L);
    }

    @Test
    void saveVacancy_ShouldReturnSavedVacancy() {
        when(vacancyRepository.save(any(Vacancy.class))).thenReturn(testVacancy);

        Vacancy result = vacancyService.saveVacancy(testVacancy);

        assertEquals(testVacancy, result);
        verify(vacancyRepository, times(1)).save(testVacancy);
    }

    @Test
    void deleteVacancy_ShouldCallRepositoryDelete() {
        doNothing().when(vacancyRepository).deleteById(1L);

        vacancyService.deleteVacancy(1L);

        verify(vacancyRepository, times(1)).deleteById(1L);
    }

    @Test
    void getVacanciesByFilters_WithAllFilters_ShouldReturnFilteredVacancies() {
        when(vacancyRepository.findByFilters("Moscow", "Test Company", "Java", 50000, 150000))
                .thenReturn(Arrays.asList(testVacancy));

        List<Vacancy> result = vacancyService.getVacanciesByFilters("Moscow", "Test Company", "Java", 50000, 150000);

        assertEquals(1, result.size());
        assertEquals(testVacancy, result.get(0));
        verify(vacancyRepository, times(1)).findByFilters("Moscow", "Test Company", "Java", 50000, 150000);
    }

    @Test
    void getVacanciesByFilters_WithEmptyFilters_ShouldReturnAllVacancies() {
        when(vacancyRepository.findByFilters(null, null, null, null, null))
                .thenReturn(Arrays.asList(testVacancy, vacancy2));

        List<Vacancy> result = vacancyService.getVacanciesByFilters(null, null, null, null, null);

        assertEquals(2, result.size());
        verify(vacancyRepository, times(1)).findByFilters(null, null, null, null, null);
    }

    @Test
    void getAllCities_ShouldReturnUniqueCities() {
        when(vacancyRepository.findAllCities()).thenReturn(Arrays.asList("Moscow", "Saint Petersburg"));

        List<String> result = vacancyService.getAllCities();

        assertEquals(2, result.size());
        assertTrue(result.contains("Moscow"));
        assertTrue(result.contains("Saint Petersburg"));
        verify(vacancyRepository, times(1)).findAllCities();
    }

    @Test
    void getAllCompanies_ShouldReturnUniqueCompanies() {
        when(vacancyRepository.findAllCompanies()).thenReturn(Arrays.asList("Test Company", "Another Company"));

        List<String> result = vacancyService.getAllCompanies();

        assertEquals(2, result.size());
        assertTrue(result.contains("Test Company"));
        assertTrue(result.contains("Another Company"));
        verify(vacancyRepository, times(1)).findAllCompanies();
    }
} 