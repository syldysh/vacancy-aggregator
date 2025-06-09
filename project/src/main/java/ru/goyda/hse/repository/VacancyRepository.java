package ru.goyda.hse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.goyda.hse.model.Vacancy;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {


    @Query("SELECT v FROM Vacancy v WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.requirements) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vacancy> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT v FROM Vacancy v WHERE " +
            "(:city IS NULL OR :city = '' OR v.city = :city) AND " +
            "(:company IS NULL OR :company = '' OR v.company = :company) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.requirements) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Vacancy> findByFilters(@Param("city") String city,
                                @Param("company") String company,
                                @Param("keyword") String keyword,
                                @Param("salaryFrom") Integer salaryFrom,
                                @Param("salaryTo") Integer salaryTo);

    @Query("SELECT DISTINCT v.city FROM Vacancy v ORDER BY v.city")
    List<String> findAllCities();

    @Query("SELECT DISTINCT v.company FROM Vacancy v ORDER BY v.company")
    List<String> findAllCompanies();

    boolean existsByLinkAndSource(String link, String source);
}
