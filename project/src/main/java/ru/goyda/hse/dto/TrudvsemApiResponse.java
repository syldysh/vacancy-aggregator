package ru.goyda.hse.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrudvsemApiResponse {
    private String status;
    private RequestInfo request;
    private MetaInfo meta;
    private Results results;

    @Data
    public static class RequestInfo {
        private String api;
    }

    @Data
    public static class MetaInfo {
        private Integer total;
        private Integer limit;
    }

    @Data
    public static class Results {
        private List<VacancyWrapper> vacancies;
    }

    @Data
    public static class VacancyWrapper {
        private VacancyDto vacancy;
    }
} 