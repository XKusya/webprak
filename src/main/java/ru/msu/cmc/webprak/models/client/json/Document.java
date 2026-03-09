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
public class Document {
    private DocumentType type;
    private String series;
    private String number;
    private String issuedBy;
    private LocalDate issuedAt;
}
