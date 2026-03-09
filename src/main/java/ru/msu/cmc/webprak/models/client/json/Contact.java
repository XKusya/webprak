package ru.msu.cmc.webprak.models.client.json;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {
    private ContactType type;
    private String value;
}
