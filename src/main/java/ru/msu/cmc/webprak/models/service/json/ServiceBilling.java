package ru.msu.cmc.webprak.models.service.json;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceBilling {
    private BillingUnit unit;
    private BigDecimal basePrice;

    // MOBILE_VOICE
    private BigDecimal monthlyFee;
    private Integer includedMinutes;
    private BigDecimal callSetupFee;

    // MOBILE_INTERNET
    private Integer quotaGb;
    private BigDecimal overagePricePerGb;
    private Integer speedLimitAfterQuotaMbps;

    // SMS
    private Integer bundleSize;
    private BigDecimal bundlePrice;
}
