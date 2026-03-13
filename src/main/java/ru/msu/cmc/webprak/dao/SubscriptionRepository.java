package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.models.subscription.Subscription;
import ru.msu.cmc.webprak.models.subscription.SubscriptionStatus;

import java.sql.Timestamp;
import java.util.Collection;

public interface SubscriptionRepository extends BaseRepository<Subscription, Long> {
    Collection<Subscription> findByClientId(Long clientId);
    Collection<Subscription> findActiveByClientId(Long clientId);
    Collection<Subscription> findHistoryByClientId(Long clientId);

    Collection<Subscription> findByClientIdAndServiceId(Long clientId, Long serviceId);
    boolean existsActiveByClientIdAndServiceId(Long clientId, Long serviceId);
    boolean existsActiveByExternalId(String externalId);

    Collection<Subscription> findByStatus(SubscriptionStatus status);
    Collection<Subscription> findEndingAfter(Timestamp moment);

    Collection<Subscription> findByServiceId(Long serviceId);
}