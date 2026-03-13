package ru.msu.cmc.webprak.models.subscription.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionParams {
    private String tariff;
    private Boolean callerIdEnabled;
    private Integer quotaGb;
    private Boolean autoRenew;
    private String senderName;
}
