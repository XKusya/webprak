package ru.msu.cmc.webprak.dao.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.SubscriptionRepository;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.client.Client;
import ru.msu.cmc.webprak.models.client.ClientType;
import ru.msu.cmc.webprak.models.service.Service;
import ru.msu.cmc.webprak.models.servicetype.ServiceType;
import ru.msu.cmc.webprak.models.subscription.Subscription;
import ru.msu.cmc.webprak.models.subscription.SubscriptionStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SubscriptionRepositoryImplTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EntityManager entityManager;

    private Client client1;
    private Client client2;
    private Service service1;
    private Service service2;
    private Subscription activeSubscription;
    private Subscription endedSubscription;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Operation").executeUpdate();
        entityManager.createQuery("DELETE FROM Subscription").executeUpdate();
        entityManager.createQuery("DELETE FROM Service").executeUpdate();
        entityManager.createQuery("DELETE FROM ServiceType").executeUpdate();
        entityManager.createQuery("DELETE FROM Account").executeUpdate();
        entityManager.createQuery("DELETE FROM Client").executeUpdate();

        client1 = new Client();
        client1.setName("Ivan");
        client1.setClientType(ClientType.PERSON);

        Account account1 = new Account();
        account1.setClient(client1);
        account1.setBalance(new BigDecimal("100.00"));
        account1.setCreditLimit(new BigDecimal("50.00"));
        client1.setAccount(account1);

        entityManager.persist(client1);
        entityManager.persist(account1);

        client2 = new Client();
        client2.setName("Petr");
        client2.setClientType(ClientType.PERSON);

        Account account2 = new Account();
        account2.setClient(client2);
        account2.setBalance(new BigDecimal("50.00"));
        account2.setCreditLimit(new BigDecimal("50.00"));
        client2.setAccount(account2);

        entityManager.persist(client2);
        entityManager.persist(account2);

        ServiceType type = new ServiceType();
        type.setName("MOBILE_INTERNET");
        entityManager.persist(type);

        service1 = new Service();
        service1.setServiceType(type);
        service1.setName("Internet 20GB");
        service1.setDescription("Tariff 20GB");
        service1.setIsActive(true);
        entityManager.persist(service1);

        service2 = new Service();
        service2.setServiceType(type);
        service2.setName("Internet 50GB");
        service2.setDescription("Tariff 50GB");
        service2.setIsActive(true);
        entityManager.persist(service2);

        activeSubscription = new Subscription();
        activeSubscription.setClient(client1);
        activeSubscription.setService(service1);
        activeSubscription.setStartedAt(Timestamp.from(Instant.parse("2024-01-01T10:00:00Z")));
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setExternalId("79990000001");
        entityManager.persist(activeSubscription);

        endedSubscription = new Subscription();
        endedSubscription.setClient(client1);
        endedSubscription.setService(service2);
        endedSubscription.setStartedAt(Timestamp.from(Instant.parse("2023-01-01T10:00:00Z")));
        endedSubscription.setEndedAt(Timestamp.from(Instant.parse("2023-12-01T10:00:00Z")));
        endedSubscription.setStatus(SubscriptionStatus.ENDED);
        endedSubscription.setExternalId("79990000002");
        entityManager.persist(endedSubscription);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getById_shouldReturnSubscription_whenExists() {
        Subscription result = subscriptionRepository.getById(activeSubscription.getId());

        assertNotNull(result);
        assertEquals(activeSubscription.getId(), result.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotExists() {
        assertNull(subscriptionRepository.getById(-1L));
    }

    @Test
    void getAll_shouldReturnAllSubscriptions() {
        Collection<Subscription> result = subscriptionRepository.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void save_shouldPersistSubscription() {
        Subscription subscription = new Subscription();
        subscription.setClient(client2);
        subscription.setService(service1);
        subscription.setStartedAt(Timestamp.from(Instant.parse("2024-03-01T10:00:00Z")));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setExternalId("79990000003");

        subscriptionRepository.save(subscription);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(subscription.getId());

        Subscription persisted = entityManager.find(Subscription.class, subscription.getId());
        assertNotNull(persisted);
        assertEquals("79990000003", persisted.getExternalId());
    }

    @Test
    void update_shouldModifySubscription() {
        Subscription subscription = entityManager.find(Subscription.class, activeSubscription.getId());
        subscription.setStatus(SubscriptionStatus.ENDED);

        subscriptionRepository.update(subscription);
        entityManager.flush();
        entityManager.clear();

        Subscription updated = entityManager.find(Subscription.class, activeSubscription.getId());
        assertEquals(SubscriptionStatus.ENDED, updated.getStatus());
    }

    @Test
    void delete_shouldRemoveSubscription() {
        Subscription subscription = entityManager.find(Subscription.class, endedSubscription.getId());

        subscriptionRepository.delete(subscription);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Subscription.class, endedSubscription.getId()));
    }

    @Test
    void findByClientId_shouldReturnClientSubscriptions() {
        Collection<Subscription> result = subscriptionRepository.findByClientId(client1.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findActiveByClientId_shouldReturnActiveOnly() {
        Collection<Subscription> result = subscriptionRepository.findActiveByClientId(client1.getId());

        assertEquals(1, result.size());
        assertEquals(SubscriptionStatus.ACTIVE, result.iterator().next().getStatus());
    }

    @Test
    void findHistoryByClientId_shouldReturnEndedOnly() {
        Collection<Subscription> result = subscriptionRepository.findHistoryByClientId(client1.getId());

        assertEquals(1, result.size());
        assertEquals(SubscriptionStatus.ENDED, result.iterator().next().getStatus());
    }

    @Test
    void findByClientIdAndServiceId_shouldReturnMatches() {
        Collection<Subscription> result =
                subscriptionRepository.findByClientIdAndServiceId(client1.getId(), service1.getId());

        assertEquals(1, result.size());
        assertEquals(service1.getId(), result.iterator().next().getService().getId());
    }

    @Test
    void existsActiveByClientIdAndServiceId_shouldReturnTrue_whenExists() {
        assertTrue(subscriptionRepository.existsActiveByClientIdAndServiceId(client1.getId(), service1.getId()));
    }

    @Test
    void existsActiveByClientIdAndServiceId_shouldReturnFalse_whenNotExists() {
        assertFalse(subscriptionRepository.existsActiveByClientIdAndServiceId(client1.getId(), service2.getId()));
    }

    @Test
    void existsActiveByExternalId_shouldReturnTrue_whenExists() {
        assertTrue(subscriptionRepository.existsActiveByExternalId("79990000001"));
    }

    @Test
    void existsActiveByExternalId_shouldReturnFalse_whenNotExists() {
        assertFalse(subscriptionRepository.existsActiveByExternalId("000000"));
    }

    @Test
    void findByStatus_shouldReturnMatchingStatus() {
        Collection<Subscription> result = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals(SubscriptionStatus.ACTIVE, result.iterator().next().getStatus());
    }

    @Test
    void findEndingAfter_shouldReturnMatchingSubscriptions() {
        Collection<Subscription> result = subscriptionRepository.findEndingAfter(
                Timestamp.from(Instant.parse("2023-01-01T00:00:00Z"))
        );

        assertEquals(1, result.size());
        assertEquals(endedSubscription.getId(), result.iterator().next().getId());
    }

    @Test
    void findByServiceId_shouldReturnMatchingSubscriptions() {
        Collection<Subscription> result = subscriptionRepository.findByServiceId(service1.getId());

        assertEquals(1, result.size());
        assertEquals(service1.getId(), result.iterator().next().getService().getId());
    }
}