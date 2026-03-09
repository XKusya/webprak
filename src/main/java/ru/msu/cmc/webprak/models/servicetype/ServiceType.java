package ru.msu.cmc.webprak.models.servicetype;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicetype")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;
}
