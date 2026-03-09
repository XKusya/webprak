package ru.msu.cmc.webprak.models.operation;

import jakarta.persistence.*;
import lombok.*;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.clientservice.ClientService;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "Operation")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "client_service_id")
    private ClientService clientService;

    @Enumerated(EnumType.STRING)
    @Column(name = "op_type", nullable = false)
    @NonNull
    private OperationType opType;

    @Column(name = "op_time", nullable = false)
    @NonNull
    private Timestamp opTime;

    @Column(name = "amount", nullable = false)
    @NonNull
    private BigDecimal amount;

    @Column(name = "description")
    private String description;
}
