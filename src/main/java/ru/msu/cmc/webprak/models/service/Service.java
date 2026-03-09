package ru.msu.cmc.webprak.models.service;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.models.service.json.ServiceBilling;
import ru.msu.cmc.webprak.models.servicetype.ServiceType;

@Entity
@Table(name = "service")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @Column(name = "description")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing", columnDefinition = "jsonb")
    private ServiceBilling billing;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
