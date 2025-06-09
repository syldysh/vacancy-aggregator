package ru.goyda.hse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.goyda.hse.dto.TrudvsemApiResponse;
import ru.goyda.hse.dto.VacancyDto;
import ru.goyda.hse.model.Vacancy;
import ru.goyda.hse.repository.VacancyRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrudvsemParserService {

    private final VacancyRepository vacancyRepository;
    private final RestTemplate restTemplate;

    private static final String API_URL = "http://opendata.trudvsem.ru/api/v1/vacancies";
    private static final String SOURCE_URL = "https://trudvsem.ru/";
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 100;

    public void parseAndSaveVacancies() {
        parseAndSaveVacancies(1);
    }

    public void parseAndSaveVacancies(int pagesToParse) {
        log.info("Starting to parse vacancies from trudvsem.ru API, pages: {}...", pagesToParse);

        int totalSaved = 0;
        int totalSkipped = 0;

        for (int page = 0; page < Math.min(pagesToParse, MAX_PAGES); page++) {
            int offset = page * PAGE_SIZE;

            log.info("Parsing page {} (offset: {}, limit: {})", page + 1, offset, PAGE_SIZE);

            try {
                // Формируем URL с параметрами пагинации
                String url = String.format("%s?limit=%d&offset=%d", API_URL, PAGE_SIZE, offset);

                // Получаем данные с API
                TrudvsemApiResponse response = restTemplate.getForObject(url, TrudvsemApiResponse.class);

                if (response == null || response.getResults() == null || response.getResults().getVacancies() == null) {
                    log.warn("No vacancies found in API response for page {}", page + 1);
                    continue;
                }

                List<TrudvsemApiResponse.VacancyWrapper> vacancyWrappers = response.getResults().getVacancies();
                log.info("Found {} vacancies on page {}", vacancyWrappers.size(), page + 1);

                // Если на странице нет вакансий, прекращаем парсинг
                if (vacancyWrappers.isEmpty()) {
                    log.info("No more vacancies found, stopping pagination");
                    break;
                }

                int savedCount = 0;
                int skippedCount = 0;

                for (TrudvsemApiResponse.VacancyWrapper wrapper : vacancyWrappers) {
                    try {
                        VacancyDto vacancyDto = wrapper.getVacancy();

                        if (vacancyDto == null) {
                            log.warn("Vacancy DTO is null, skipping");
                            skippedCount++;
                            continue;
                        }

                        // Проверяем, существует ли уже такая вакансия
                        if (vacancyRepository.existsByLinkAndSource(vacancyDto.getVacUrl(), SOURCE_URL)) {
                            log.debug("Vacancy already exists: {}", vacancyDto.getVacUrl());
                            skippedCount++;
                            continue;
                        }

                        // Конвертируем DTO в сущность
                        Vacancy vacancy = convertToVacancy(vacancyDto);

                        if (vacancy != null) {
                            vacancyRepository.save(vacancy);
                            savedCount++;
                        } else {
                            skippedCount++;
                        }

                    } catch (Exception e) {
                        log.error("Error processing vacancy: {}", e.getMessage(), e);
                        skippedCount++;
                    }
                }

                totalSaved += savedCount;
                totalSkipped += skippedCount;

                log.info("Page {} completed. Saved: {}, Skipped: {}", page + 1, savedCount, skippedCount);

                // Если на странице меньше вакансий, чем максимум, значит это последняя страница
                if (vacancyWrappers.size() < PAGE_SIZE) {
                    log.info("Last page reached (found {} vacancies, expected {})", vacancyWrappers.size(), PAGE_SIZE);
                    break;
                }

            } catch (Exception e) {
                log.error("Error parsing page {} from trudvsem.ru API: {}", page + 1, e.getMessage(), e);
            }
        }

        log.info("Parsing completed. Total saved: {}, Total skipped: {}", totalSaved, totalSkipped);
    }

    private Vacancy convertToVacancy(VacancyDto dto) {
        try {
            Vacancy vacancy = new Vacancy();

            // Обязательные поля
            vacancy.setTitle(dto.getJobName() != null ? dto.getJobName() : "Не указано");
            vacancy.setCompany(dto.getCompany() != null && dto.getCompany().getName() != null
                    ? dto.getCompany().getName() : "Не указано");

            // Город из региона или адреса
            String city = extractCity(dto);
            vacancy.setCity(city != null ? city : "Не указано");

            // Зарплата
            vacancy.setSalary(dto.getSalary() != null ? dto.getSalary() : null);

            // Описание (обязанности)
            vacancy.setDescription(dto.getDuty() != null ? dto.getDuty() : "Описание не указано");

            // Требования
            String requirements = extractRequirements(dto);
            vacancy.setRequirements(requirements != null ? requirements : "Требования не указаны");

            // Дата публикации
            LocalDateTime publicationDate = parseDate(dto.getCreationDate());
            vacancy.setPublicationDate(publicationDate != null ? publicationDate : LocalDateTime.now());

            // Ссылка
            vacancy.setLink(dto.getVacUrl() != null ? dto.getVacUrl() : "");

            // Источник
            vacancy.setSource(SOURCE_URL);

            return vacancy;

        } catch (Exception e) {
            log.error("Error converting vacancy DTO to entity: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extractCity(VacancyDto dto) {
        // Сначала пробуем получить из региона
        if (dto.getRegion() != null && dto.getRegion().getName() != null) {
            return dto.getRegion().getName();
        }

        // Если нет, пробуем из адреса
        if (dto.getAddresses() != null && dto.getAddresses().getAddress() != null
                && !dto.getAddresses().getAddress().isEmpty()) {
            String location = dto.getAddresses().getAddress().get(0).getLocation();
            if (location != null) {
                // Пытаемся извлечь город из строки адреса
                String[] parts = location.split(",");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("г ")) {
                        return part.substring(2).trim();
                    }
                }
                // Если не нашли город, возвращаем первую часть адреса
                return parts.length > 0 ? parts[0].trim() : null;
            }
        }

        return null;
    }

    private String extractRequirements(VacancyDto dto) {
        StringBuilder requirements = new StringBuilder();

        if (dto.getRequirement() != null) {
            if (dto.getRequirement().getEducation() != null) {
                requirements.append("Образование: ").append(dto.getRequirement().getEducation()).append(". ");
            }

            if (dto.getRequirement().getExperience() != null) {
                requirements.append("Опыт работы: ").append(dto.getRequirement().getExperience()).append(" лет. ");
            }

            if (dto.getRequirement().getQualification() != null) {
                requirements.append("Квалификация: ").append(dto.getRequirement().getQualification()).append(". ");
            }
        }

        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            requirements.append("Навыки: ").append(String.join(", ", dto.getSkills())).append(". ");
        }

        return requirements.length() > 0 ? requirements.toString().trim() : null;
    }

    private LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            //  парсим дату в формате yyyy-MM-dd
            return LocalDateTime.parse(dateString + "T00:00:00");
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date: {}", dateString);
            return null;
        }
    }
} 