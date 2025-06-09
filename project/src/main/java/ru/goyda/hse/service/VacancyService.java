package ru.goyda.hse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.goyda.hse.model.Vacancy;
import ru.goyda.hse.repository.VacancyRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    public Optional<Vacancy> getVacancyById(Long id) {
        return vacancyRepository.findById(id);
    }

    public Vacancy saveVacancy(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }

    public void deleteVacancy(Long id) {
        vacancyRepository.deleteById(id);
    }


    public List<Vacancy> getVacanciesByFilters(String city, String company, String keyword, Integer salaryFrom, Integer salaryTo) {
        // Преобразуем пустые строки в null для корректной работы SQL запроса
        String normalizedCity = (city != null && city.trim().isEmpty()) ? null : city;
        String normalizedCompany = (company != null && company.trim().isEmpty()) ? null : company;
        String normalizedKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

        log.info("Searching vacancies with filters: city='{}', company='{}', keyword='{}', salaryFrom={}, salaryTo={}",
                normalizedCity, normalizedCompany, normalizedKeyword, salaryFrom, salaryTo);

        // Сначала получаем результаты по основным фильтрам
        List<Vacancy> result = vacancyRepository.findByFilters(normalizedCity, normalizedCompany, normalizedKeyword, salaryFrom, salaryTo);

        // Если есть фильтры по зарплате, применяем их в Java коде
        if (salaryFrom != null || salaryTo != null) {
            result = result.stream()
                    .filter(vacancy -> matchesSalaryRange(vacancy.getSalary(), salaryFrom, salaryTo))
                    .collect(java.util.stream.Collectors.toList());
        }

        log.info("Found {} vacancies matching the filters", result.size());

        return result;
    }

    private boolean matchesSalaryRange(String salary, Integer salaryFrom, Integer salaryTo) {
        if (salary == null || salary.trim().isEmpty()) {
            return false; // Если зарплата не указана, не включаем в результаты при фильтрации по зарплате
        }

        // Извлекаем числа из строки зарплаты
        String numbersOnly = salary.replaceAll("[^0-9]", "");
        if (numbersOnly.isEmpty()) {
            return false;
        }

        try {
            int salaryValue = Integer.parseInt(numbersOnly);

            // Проверяем диапазон
            if (salaryFrom != null && salaryValue < salaryFrom) {
                return false;
            }
            if (salaryTo != null && salaryValue > salaryTo) {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<String> getAllCities() {
        return vacancyRepository.findAllCities();
    }

    public List<String> getAllCompanies() {
        return vacancyRepository.findAllCompanies();
    }
}
