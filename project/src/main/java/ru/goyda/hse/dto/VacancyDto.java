package ru.goyda.hse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VacancyDto {
    private String id;
    private String source;
    private RegionDto region;
    private CompanyDto company;

    @JsonProperty("creation-date")
    private String creationDate;

    private String salary;

    @JsonProperty("salary_min")
    private Integer salaryMin;

    @JsonProperty("salary_max")
    private Integer salaryMax;

    @JsonProperty("job-name")
    private String jobName;

    @JsonProperty("vac_url")
    private String vacUrl;

    private String employment;
    private String schedule;
    private String duty;
    private CategoryDto category;
    private RequirementDto requirement;
    private AddressesDto addresses;

    @JsonProperty("social_protected")
    private String socialProtected;

    @JsonProperty("contact_list")
    private List<ContactDto> contactList;

    @JsonProperty("contact_person")
    private String contactPerson;

    @JsonProperty("work_places")
    private Integer workPlaces;

    @JsonProperty("code_profession")
    private String codeProfession;

    private List<String> skills;
    private String conditions;

    @JsonProperty("trainingDays")
    private Integer trainingDays;

    private String currency;

    @Data
    public static class RegionDto {
        @JsonProperty("region_code")
        private String regionCode;
        private String name;
    }

    @Data
    public static class CompanyDto {
        private String companycode;
        private String email;
        @JsonProperty("hr-agency")
        private Boolean hrAgency;
        private String inn;
        private String kpp;
        private String name;
        private String ogrn;
        private String site;
        private String url;
    }

    @Data
    public static class CategoryDto {
        private String specialisation;
    }

    @Data
    public static class RequirementDto {
        private String education;
        private String qualification;
        private Integer experience;
    }

    @Data
    public static class AddressesDto {
        private List<AddressDto> address;
    }

    @Data
    public static class AddressDto {
        private String location;
        private String lng;
        private String lat;
    }

    @Data
    public static class ContactDto {
        @JsonProperty("contact_type")
        private String contactType;
        @JsonProperty("contact_value")
        private String contactValue;
    }
} 