package ru.msu.cmc.webprak.dao.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprak.dao.ClientRepository;
import ru.msu.cmc.webprak.models.client.Client;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public class ClientRepositoryImpl extends BaseRepositoryImpl<Client, Long> implements ClientRepository {

    public ClientRepositoryImpl() {
        super(Client.class);
    }

    @Override
    public Collection<Client> findByNameContaining(String namePart) {
        return entityManager
                .createQuery(
                        "SELECT c FROM Client c " +
                                "WHERE LOWER(c.name) LIKE LOWER(:name)",
                        Client.class
                )
                .setParameter("name", "%" + namePart + "%")
                .getResultList();
    }

    @Override
    public Collection<Client> findByServiceAndPeriod(Long serviceId, Timestamp from, Timestamp to) {
        return entityManager
                .createQuery(
                        "SELECT DISTINCT c FROM Client c " +
                                "JOIN c.subscriptions s " +
                                "WHERE s.service.id = :serviceId " +
                                "AND s.startedAt BETWEEN :from AND :to",
                        Client.class
                )
                .setParameter("serviceId", serviceId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    @Override
    public Collection<Client> findWithNegativeBalance() {
        return entityManager
                .createQuery(
                        "SELECT c FROM Client c " +
                                "JOIN c.account a " +
                                "WHERE a.balance < 0",
                        Client.class
                )
                .getResultList();
    }

    @Override
    public Collection<Client> findWithCreditLimitExceeded() {
        return entityManager
                .createQuery(
                        "SELECT c FROM Client c " +
                                "JOIN c.account a " +
                                "WHERE a.balance < -a.creditLimit",
                        Client.class
                )
                .getResultList();
    }

    @Override
    public Collection<Client> findWithOverdueDebt(LocalDate currentDate) {
        return entityManager
                .createQuery(
                        "SELECT c FROM Client c " +
                                "JOIN c.account a " +
                                "WHERE a.debtDueDate < :date",
                        Client.class
                )
                .setParameter("date", currentDate)
                .getResultList();
    }

    @Override
    public Client findDetailedById(Long id) {
        List<Client> result = entityManager
                .createQuery(
                        "SELECT DISTINCT c FROM Client c " +
                                "LEFT JOIN FETCH c.account " +
                                "LEFT JOIN FETCH c.subscriptions " +
                                "WHERE c.id = :id",
                        Client.class
                )
                .setParameter("id", id)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public boolean canBeDeleted(Long clientId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(s) FROM Subscription s " +
                                "WHERE s.client.id = :id AND s.status = :status",
                        Long.class
                )
                .setParameter("id", clientId)
                .setParameter("status", ru.msu.cmc.webprak.models.subscription.SubscriptionStatus.ACTIVE)
                .getSingleResult();

        return count == 0;
    }
}