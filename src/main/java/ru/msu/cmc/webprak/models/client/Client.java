package ru.msu.cmc.webprak.models.client;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.client.json.ClientDetails;
import ru.msu.cmc.webprak.models.client.json.Contact;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "client")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type")
    private ClientType clientType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private ClientDetails details;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contacts", columnDefinition = "jsonb")
    private List<Contact> contacts;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @OneToOne(mappedBy = "client")
    private Account account;
}
