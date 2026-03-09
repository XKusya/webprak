package ru.msu.cmc.webprak.models.clientservice;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.models.client.Client;
import ru.msu.cmc.webprak.models.clientservice.json.ClientServiceParams;
import ru.msu.cmc.webprak.models.service.Service;

import java.sql.Timestamp;

@Entity
@Table(name = "ClientService")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientService {
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
    @Column(name = "status", nullable = false)
    @NonNull
    private ClientServiceStatus status;

    @Column(name = "external_id")
    private String externalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", columnDefinition = "jsonb")
    private ClientServiceParams params;
}
