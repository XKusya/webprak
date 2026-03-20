package ru.msu.cmc.webprak.dao.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.ClientRepository;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.client.Client;
import ru.msu.cmc.webprak.models.client.ClientType;
import ru.msu.cmc.webprak.models.service.Service;
import ru.msu.cmc.webprak.models.servicetype.ServiceType;
import ru.msu.cmc.webprak.models.subscription.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ClientRepositoryImplTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EntityManager entityManager;

    private Client client1;
    private Client client2;
    private Client client3;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Operation").executeUpdate();
        entityManager.createQuery("DELETE FROM Subscription").executeUpdate();
        entityManager.createQuery("DELETE FROM Service").executeUpdate();
        entityManager.createQuery("DELETE FROM ServiceType").executeUpdate();
        entityManager.createQuery("DELETE FROM Account").executeUpdate();
        entityManager.createQuery("DELETE FROM Client").executeUpdate();

        client1 = new Client();
        client1.setName("Ivan Petrov");
        client1.setClientType(ClientType.PERSON);
        Account account1 = new Account();
        account1.setClient(client1);
        account1.setBalance(new BigDecimal("100.00"));
        account1.setCreditLimit(new BigDecimal("50.00"));
        account1.setDebtDueDate(LocalDate.of(2030, 1, 1));
        client1.setAccount(account1);
        entityManager.persist(client1);
        entityManager.persist(account1);

        client2 = new Client();
        client2.setName("OOO Romashka");
        client2.setClientType(ClientType.ORG);
        entityManager.persist(client2);
        Account account2 = new Account();
        account2.setClient(client2);
        account2.setBalance(new BigDecimal("-20.00"));
        account2.setCreditLimit(new BigDecimal("100.00"));
        account2.setDebtDueDate(LocalDate.of(2030, 1, 1));
        entityManager.persist(account2);

        client3 = new Client();
        client3.setName("Overdue Client");
        client3.setClientType(ClientType.PERSON);
        entityManager.persist(client3);
        Account account3 = new Account();
        account3.setClient(client3);
        account3.setBalance(new BigDecimal("-200.00"));
        account3.setCreditLimit(new BigDecimal("100.00"));
        account3.setDebtDueDate(LocalDate.of(2020, 1, 1));
        entityManager.persist(account3);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getById_shouldReturnClient_whenExists() {
        Client result = clientRepository.getById(client1.getId());

        assertNotNull(result);
        assertEquals("Ivan Petrov", result.getName());
    }

    @Test
    void getById_shouldReturnNull_whenNotExists() {
        assertNull(clientRepository.getById(-1L));
    }

    @Test
    void getAll_shouldReturnAllClients() {
        Collection<Client> result = clientRepository.getAll();

        assertEquals(3, result.size());
    }

    @Test
    void save_shouldPersistClient() {
        Client client = new Client();
        client.setName("New Client");

        clientRepository.save(client);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(client.getId());
        assertEquals("New Client", entityManager.find(Client.class, client.getId()).getName());
    }

    @Test
    void update_shouldModifyClient() {
        Client client = entityManager.find(Client.class, client1.getId());
        client.setName("Updated Name");

        clientRepository.update(client);
        entityManager.flush();
        entityManager.clear();

        assertEquals("Updated Name", entityManager.find(Client.class, client1.getId()).getName());
    }

    @Test
    void delete_shouldRemoveClient() {
        Client client = entityManager.find(Client.class, client1.getId());

        clientRepository.delete(client);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Client.class, client1.getId()));
    }

    @Test
    void findByNameContaining_shouldReturnMatches() {
        Collection<Client> result = clientRepository.findByNameContaining("ivan");

        assertEquals(1, result.size());
        assertEquals("Ivan Petrov", result.iterator().next().getName());
    }

    @Test
    void findByNameContaining_shouldReturnEmpty_whenNoMatches() {
        assertTrue(clientRepository.findByNameContaining("zzz").isEmpty());
    }

    @Test
    void findWithNegativeBalance_shouldReturnClients() {
        Collection<Client> result = clientRepository.findWithNegativeBalance();

        assertEquals(2, result.size());
    }

    @Test
    void findWithCreditLimitExceeded_shouldReturnExceededOnly() {
        Collection<Client> result = clientRepository.findWithCreditLimitExceeded();

        assertEquals(1, result.size());
        assertEquals("Overdue Client", result.iterator().next().getName());
    }

    @Test
    void findWithOverdueDebt_shouldReturnOverdueOnly() {
        Collection<Client> result = clientRepository.findWithOverdueDebt(LocalDate.of(2024, 1, 1));

        assertEquals(1, result.size());
        assertEquals("Overdue Client", result.iterator().next().getName());
    }

    @Test
    void findDetailedById_shouldReturnClient_whenExists() {
        Client result = clientRepository.findDetailedById(client1.getId());

        assertNotNull(result);
        assertEquals("Ivan Petrov", result.getName());
        assertNotNull(result.getAccount());
    }

    @Test
    void findDetailedById_shouldReturnNull_whenNotExists() {
        assertNull(clientRepository.findDetailedById(-1L));
    }

    @Test
    void canBeDeleted_shouldReturnTrue_whenNoActiveSubscriptions() {
        assertTrue(clientRepository.canBeDeleted(client1.getId()));
    }

    @Test
    void canBeDeleted_shouldReturnFalse_whenHasActiveSubscription() {
        String typeName = "SMS-" + System.nanoTime();
        String serviceName = "SMS Pack-" + System.nanoTime();

        ServiceType type = new ServiceType();
        type.setName(typeName);
        entityManager.persist(type);

        Service service =  new Service();
        service.setServiceType(type);
        service.setName(serviceName);
        service.setDescription("SMS package");
        service.setIsActive(true);
        entityManager.persist(service);

        Subscription subscription = new Subscription();
        subscription.setClient(client1);
        subscription.setService(service);
        subscription.setStartedAt(java.sql.Timestamp.from(java.time.Instant.parse("2024-01-01T10:00:00Z")));
        subscription.setStatus(ru.msu.cmc.webprak.models.subscription.SubscriptionStatus.ACTIVE);
        subscription.setExternalId("79990000001");
        entityManager.persist(subscription);
        entityManager.flush();

        assertFalse(clientRepository.canBeDeleted(client1.getId()));
    }
}
