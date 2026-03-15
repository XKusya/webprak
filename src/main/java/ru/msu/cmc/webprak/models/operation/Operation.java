package ru.msu.cmc.webprak.models.operation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.subscription.Subscription;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "operation")
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
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "op_type", nullable = false, columnDefinition = "OperationType")
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
