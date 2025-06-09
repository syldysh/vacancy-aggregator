package ru.goyda.hse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import ru.goyda.hse.dto.TrudvsemApiResponse;
import ru.goyda.hse.dto.VacancyDto;
import ru.goyda.hse.model.Vacancy;
import ru.goyda.hse.repository.VacancyRepository;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrudvsemParserServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private VacancyService vacancyService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TrudvsemParserService trudvsemParserService;

    private TrudvsemApiResponse mockApiResponse;
    private VacancyDto mockVacancyDto;

    @BeforeEach
    void setUp() {
        // Создаем мок DTO
        mockVacancyDto = new VacancyDto();
        mockVacancyDto.setJobName("Java Developer");
        mockVacancyDto.setVacUrl("https://trudvsem.ru/vacancy/123");
        mockVacancyDto.setDuty("Разработка Java приложений");
        mockVacancyDto.setSalary("100000");
        mockVacancyDto.setCreationDate("2024-01-15");

        // Создаем мок компании
        VacancyDto.CompanyDto company = new VacancyDto.CompanyDto();
        company.setName("Tech Company");
        mockVacancyDto.setCompany(company);

        // Создаем мок региона
        VacancyDto.RegionDto region = new VacancyDto.RegionDto();
        region.setName("Москва");
        mockVacancyDto.setRegion(region);

        // Создаем мок требований
        VacancyDto.RequirementDto requirement = new VacancyDto.RequirementDto();
        requirement.setEducation("Высшее");
        requirement.setExperience(3);
        requirement.setQualification("Java Developer");
        mockVacancyDto.setRequirement(requirement);

        // Создаем мок навыков
        mockVacancyDto.setSkills(Arrays.asList("Java", "Spring", "SQL"));

        // Создаем мок wrapper
        TrudvsemApiResponse.VacancyWrapper wrapper = new TrudvsemApiResponse.VacancyWrapper();
        wrapper.setVacancy(mockVacancyDto);

        // Создаем мок results
        TrudvsemApiResponse.Results results = new TrudvsemApiResponse.Results();
        results.setVacancies(Arrays.asList(wrapper));

        // Создаем мок API response
        mockApiResponse = new TrudvsemApiResponse();
        mockApiResponse.setResults(results);
    }

    @Test
    void parseAndSaveVacancies_WithDefaultPages_ShouldParseOnePage() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(mockApiResponse);
        when(vacancyRepository.existsByLinkAndSource(anyString(), anyString()))
                .thenReturn(false);
        when(vacancyRepository.save(any(Vacancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        trudvsemParserService.parseAndSaveVacancies();

        // Then
        verify(restTemplate).getForObject(
                eq("http://opendata.trudvsem.ru/api/v1/vacancies?limit=100&offset=0"),
                eq(TrudvsemApiResponse.class)
        );
        verify(vacancyRepository).save(any(Vacancy.class));
    }

    @Test
    void parseAndSaveVacancies_WithMultiplePages_ShouldParseAllPages() {
        // Given
        // Создаем ответ с полным размером страницы (100 вакансий)
        TrudvsemApiResponse.VacancyWrapper[] wrappers = new TrudvsemApiResponse.VacancyWrapper[100];
        for (int i = 0; i < 100; i++) {
            TrudvsemApiResponse.VacancyWrapper wrapper = new TrudvsemApiResponse.VacancyWrapper();
            VacancyDto dto = new VacancyDto();
            dto.setJobName("Developer " + i);
            dto.setVacUrl("https://trudvsem.ru/vacancy/" + i);
            wrapper.setVacancy(dto);
            wrappers[i] = wrapper;
        }

        TrudvsemApiResponse.Results fullResults = new TrudvsemApiResponse.Results();
        fullResults.setVacancies(java.util.Arrays.asList(wrappers));

        TrudvsemApiResponse fullResponse = new TrudvsemApiResponse();
        fullResponse.setResults(fullResults);

        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(fullResponse);
        when(vacancyRepository.existsByLinkAndSource(anyString(), anyString()))
                .thenReturn(false);
        when(vacancyRepository.save(any(Vacancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        trudvsemParserService.parseAndSaveVacancies(3);

        // Then
        verify(restTemplate).getForObject(
                eq("http://opendata.trudvsem.ru/api/v1/vacancies?limit=100&offset=0"),
                eq(TrudvsemApiResponse.class)
        );
        verify(restTemplate).getForObject(
                eq("http://opendata.trudvsem.ru/api/v1/vacancies?limit=100&offset=100"),
                eq(TrudvsemApiResponse.class)
        );
        verify(restTemplate).getForObject(
                eq("http://opendata.trudvsem.ru/api/v1/vacancies?limit=100&offset=200"),
                eq(TrudvsemApiResponse.class)
        );
        verify(vacancyRepository, times(300)).save(any(Vacancy.class)); // 100 * 3 pages
    }

    @Test
    void parseAndSaveVacancies_WhenVacancyAlreadyExists_ShouldSkipSaving() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(mockApiResponse);
        when(vacancyRepository.existsByLinkAndSource(anyString(), anyString()))
                .thenReturn(true); // Вакансия уже существует

        // When
        trudvsemParserService.parseAndSaveVacancies();

        // Then
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void parseAndSaveVacancies_WhenApiReturnsNull_ShouldHandleGracefully() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(null);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> trudvsemParserService.parseAndSaveVacancies());

        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void parseAndSaveVacancies_WhenApiReturnsEmptyResults_ShouldHandleGracefully() {
        // Given
        TrudvsemApiResponse emptyResponse = new TrudvsemApiResponse();
        TrudvsemApiResponse.Results emptyResults = new TrudvsemApiResponse.Results();
        emptyResults.setVacancies(Collections.emptyList());
        emptyResponse.setResults(emptyResults);

        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(emptyResponse);

        // When
        trudvsemParserService.parseAndSaveVacancies();

        // Then
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void parseAndSaveVacancies_WhenRestTemplateThrowsException_ShouldHandleGracefully() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> trudvsemParserService.parseAndSaveVacancies());

        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void parseAndSaveVacancies_WithMaxPagesExceeded_ShouldLimitToMaxPages() {
        // Given
        // Создаем ответ с полным размером страницы (100 вакансий)
        TrudvsemApiResponse.VacancyWrapper[] wrappers = new TrudvsemApiResponse.VacancyWrapper[100];
        for (int i = 0; i < 100; i++) {
            TrudvsemApiResponse.VacancyWrapper wrapper = new TrudvsemApiResponse.VacancyWrapper();
            VacancyDto dto = new VacancyDto();
            dto.setJobName("Developer " + i);
            dto.setVacUrl("https://trudvsem.ru/vacancy/" + i);
            wrapper.setVacancy(dto);
            wrappers[i] = wrapper;
        }

        TrudvsemApiResponse.Results fullResults = new TrudvsemApiResponse.Results();
        fullResults.setVacancies(java.util.Arrays.asList(wrappers));

        TrudvsemApiResponse fullResponse = new TrudvsemApiResponse();
        fullResponse.setResults(fullResults);

        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(fullResponse);
        when(vacancyRepository.existsByLinkAndSource(anyString(), anyString()))
                .thenReturn(false);
        when(vacancyRepository.save(any(Vacancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - запрашиваем больше максимума (150 > 100)
        trudvsemParserService.parseAndSaveVacancies(150);

        // Then - должно быть обработано только 100 страниц
        verify(restTemplate, times(100)).getForObject(anyString(), eq(TrudvsemApiResponse.class));
        verify(vacancyRepository, times(10000)).save(any(Vacancy.class)); // 100 * 100 pages
    }

    @Test
    void parseAndSaveVacancies_WhenLessThanPageSizeReturned_ShouldStopPagination() {
        // Given
        // Создаем ответ с меньшим количеством вакансий, чем размер страницы
        TrudvsemApiResponse.VacancyWrapper wrapper = new TrudvsemApiResponse.VacancyWrapper();
        wrapper.setVacancy(mockVacancyDto);

        TrudvsemApiResponse.Results results = new TrudvsemApiResponse.Results();
        results.setVacancies(Arrays.asList(wrapper)); // Только 1 вакансия (< 100)

        TrudvsemApiResponse smallResponse = new TrudvsemApiResponse();
        smallResponse.setResults(results);

        when(restTemplate.getForObject(anyString(), eq(TrudvsemApiResponse.class)))
                .thenReturn(smallResponse);
        when(vacancyRepository.existsByLinkAndSource(anyString(), anyString()))
                .thenReturn(false);
        when(vacancyRepository.save(any(Vacancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - запрашиваем 5 страниц
        trudvsemParserService.parseAndSaveVacancies(5);

        // Then - должна быть обработана только 1 страница (так как вернулось меньше 100 вакансий)
        verify(restTemplate, times(1)).getForObject(anyString(), eq(TrudvsemApiResponse.class));
        verify(vacancyRepository, times(1)).save(any(Vacancy.class));
    }
} 