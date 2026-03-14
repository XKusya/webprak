package ru.msu.cmc.webprak.dao.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprak.dao.SubscriptionRepository;
import ru.msu.cmc.webprak.models.subscription.Subscription;
import ru.msu.cmc.webprak.models.subscription.SubscriptionStatus;

import java.sql.Timestamp;
import java.util.Collection;

@Repository
@Transactional
public class SubscriptionRepositoryImpl extends BaseRepositoryImpl<Subscription, Long> implements SubscriptionRepository {

    public SubscriptionRepositoryImpl() {
        super(Subscription.class);
    }

    @Override
    public Collection<Subscription> findByClientId(Long clientId) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.client.id = :clientId " +
                                "ORDER BY s.startedAt DESC",
                        Subscription.class
                )
                .setParameter("clientId", clientId)
                .getResultList();
    }

    @Override
    public Collection<Subscription> findActiveByClientId(Long clientId) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.client.id = :clientId " +
                                "AND s.status = :status " +
                                "ORDER BY s.startedAt DESC",
                        Subscription.class
                )
                .setParameter("clientId", clientId)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getResultList();
    }

    @Override
    public Collection<Subscription> findHistoryByClientId(Long clientId) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.client.id = :clientId " +
                                "AND s.status = :status " +
                                "ORDER BY s.startedAt DESC",
                        Subscription.class
                )
                .setParameter("clientId", clientId)
                .setParameter("status", SubscriptionStatus.ENDED)
                .getResultList();
    }

    @Override
    public Collection<Subscription> findByClientIdAndServiceId(Long clientId, Long serviceId) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.client.id = :clientId " +
                                "AND s.service.id = :serviceId " +
                                "ORDER BY s.startedAt DESC",
                        Subscription.class
                )
                .setParameter("clientId", clientId)
                .setParameter("serviceId", serviceId)
                .getResultList();
    }

    @Override
    public boolean existsActiveByClientIdAndServiceId(Long clientId, Long serviceId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(s) FROM Subscription s " +
                                "WHERE s.client.id = :clientId " +
                                "AND s.service.id = :serviceId " +
                                "AND s.status = :status",
                        Long.class
                )
                .setParameter("clientId", clientId)
                .setParameter("serviceId", serviceId)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean existsActiveByExternalId(String externalId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(s) FROM Subscription s " +
                                "WHERE s.externalId = :externalId " +
                                "AND s.status = :status",
                        Long.class
                )
                .setParameter("externalId", externalId)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Collection<Subscription> findByStatus(SubscriptionStatus status) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.status = :status " +
                                "ORDER BY s.startedAt DESC",
                        Subscription.class
                )
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public Collection<Subscription> findEndingAfter(Timestamp moment) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.endedAt IS NOT NULL " +
                                "AND s.endedAt > :moment " +
                                "ORDER BY s.endedAt ASC",
                        Subscription.class
                )
                .setParameter("moment", moment)
                .getResultList();
    }

    @Override
    public Collection<Subscription> findByServiceId(Long serviceId) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Subscription s " +
                                "WHERE s.service.id = :serviceId " +
                                "ORDER BY s.startedAt DESC",
                        Subscription.class
                )
                .setParameter("serviceId", serviceId)
                .getResultList();
    }
}