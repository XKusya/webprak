package ru.msu.cmc.webprak.models.subscription;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.models.client.Client;
import ru.msu.cmc.webprak.models.subscription.json.SubscriptionParams;
import ru.msu.cmc.webprak.models.service.Service;

import java.sql.Timestamp;

@Entity
@Table(name = "Subscription")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "started_at", nullable = false)
    @NonNull
    private Timestamp startedAt;

    @Column(name = "ended_at")
    private Timestamp endedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "SubscriptionStatus")
    @NonNull
    private SubscriptionStatus status;

    @Column(name = "external_id")
    private String externalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", columnDefinition = "jsonb")
    private SubscriptionParams params;
}
