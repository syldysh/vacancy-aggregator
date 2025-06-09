package ru.goyda.hse.controller;
/*
контроллер - компонент фреймворка, который отвечает за обработку входящих HTTP-запросов
и возвращение соответствующих ответов
*/
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.goyda.hse.model.Vacancy;
import ru.goyda.hse.service.TrudvsemParserService;
import ru.goyda.hse.service.VacancyService;

import java.util.List;

@RestController
// @RestController обозначает, что класс является контроллером, и возвращает данные в HTTP-ответе.
@RequestMapping("/api")
// @RequestMapping("/api") задаёт базовый URL /api.
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VacancyController {

    private final VacancyService vacancyService;
    private final TrudvsemParserService trudvsemParserService;

    @GetMapping("/vacancies")
//   @GetMapping("/vacancies") обрабатывает GET-запрос по адресу /api/vacancies и возвращает ответ.
    public ResponseEntity<List<Vacancy>> getAllVacancies() {
        List<Vacancy> vacancies = vacancyService.getAllVacancies();
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/vacancies/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {
        return vacancyService.getVacancyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vacancies/search")
    public ResponseEntity<List<Vacancy>> searchVacancies(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer salaryFrom,
            @RequestParam(required = false) Integer salaryTo) {

        try {
            List<Vacancy> vacancies = vacancyService.getVacanciesByFilters(city, company, keyword, salaryFrom, salaryTo);
            return ResponseEntity.ok(vacancies);
        } catch (Exception e) {
            // логируем ошибку для отладки
            System.err.println("Error in searchVacancies: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getAllCities() {
        List<String> cities = vacancyService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<String>> getAllCompanies() {
        List<String> companies = vacancyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }


    @PostMapping("/parse-vacancies")
    public ResponseEntity<String> parseVacancies(@RequestParam(defaultValue = "1") int pages) {
        try {
            if (pages < 1 || pages > 100) {
                return ResponseEntity.badRequest().body("Pages parameter must be between 1 and 100");
            }

            trudvsemParserService.parseAndSaveVacancies(pages);
            return ResponseEntity.ok("Parsing completed successfully for " + pages + " page(s)");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during parsing: " + e.getMessage());
        }
    }
}
