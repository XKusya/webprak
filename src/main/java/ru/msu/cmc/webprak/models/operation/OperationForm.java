package ru.msu.cmc.webprak.models.operation;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OperationForm {
    private Long clientId;
    private Long subscriptionId;
    private OperationType opType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime opTime;

    private BigDecimal amount;
    private String description;
}
