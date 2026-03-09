package ru.msu.cmc.webprak.models.account;

import jakarta.persistence.*;
import lombok.*;
import ru.msu.cmc.webprak.models.client.Client;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "debt_due_date")
    private LocalDate debtDueDate;
}
