package ru.msu.cmc.webprak.models.client.json;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientDetails {
    // ORG
    private String legalName;
    private String inn;
    private String kpp;
    private String ogrn;
    private String legalAddress;

    // PERSON
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate birthDate;

    @JsonProperty("sex")
    private Gender gender;

    private Document document;

    @JsonProperty("registrationsAddress")
    private String registrationsAddress;
}
