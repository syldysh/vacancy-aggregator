package ru.goyda.hse.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.goyda.hse.service.TrudvsemParserService;
import ru.goyda.hse.service.VacancyService;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyDataInitializer implements CommandLineRunner {

    private final TrudvsemParserService trudvsemParserService;
    private final VacancyService vacancyService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting Vacancy Data Initialization ===");

        try {
            // Сначала парсим данные с API
            log.info("Step 1: Parsing vacancies from trudvsem.ru API...");
            trudvsemParserService.parseAndSaveVacancies(1);

            // Проверяем, сколько вакансий у нас есть
            long vacancyCount = vacancyService.getAllVacancies().size();
            log.info("Total vacancies in database after parsing: {}", vacancyCount);

            if (vacancyCount == 0) {
                log.warn("No vacancies found after parsing. This might indicate an API issue.");
            }

        } catch (Exception e) {
            log.error("Error during vacancy data initialization", e);
            // Не прерываем запуск приложения, если парсинг не удался
        }

        log.info("=== Vacancy Data Initialization Completed ===");
    }
} 