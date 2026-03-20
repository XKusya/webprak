package ru.msu.cmc.webprak.dao.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.OperationRepository;
import ru.msu.cmc.webprak.models.account.Account;
import ru.msu.cmc.webprak.models.client.Client;
import ru.msu.cmc.webprak.models.client.ClientType;
import ru.msu.cmc.webprak.models.operation.Operation;
import ru.msu.cmc.webprak.models.operation.OperationType;
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
class OperationRepositoryImplTest {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private EntityManager entityManager;

    private Account account;
    private Subscription subscription;
    private Operation payment;
    private Operation charge;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Operation").executeUpdate();
        entityManager.createQuery("DELETE FROM Subscription").executeUpdate();
        entityManager.createQuery("DELETE FROM Service").executeUpdate();
        entityManager.createQuery("DELETE FROM ServiceType").executeUpdate();
        entityManager.createQuery("DELETE FROM Account").executeUpdate();
        entityManager.createQuery("DELETE FROM Client").executeUpdate();

        Client client = new Client();
        client.setName("Ivan");
        client.setClientType(ClientType.PERSON);

        account = new Account();
        account.setClient(client);
        account.setBalance(new BigDecimal("100.00"));
        account.setCreditLimit(new BigDecimal("50.00"));
        client.setAccount(account);

        entityManager.persist(client);
        entityManager.persist(account);

        ServiceType type = new ServiceType();
        type.setName("SMS");
        entityManager.persist(type);

        Service service = new Service();
        service.setServiceType(type);
        service.setName("SMS Pack");
        service.setDescription("SMS package");
        service.setIsActive(true);
        entityManager.persist(service);

        subscription = new Subscription();
        subscription.setClient(client);
        subscription.setService(service);
        subscription.setStartedAt(Timestamp.from(Instant.parse("2024-01-01T10:00:00Z")));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setExternalId("79990000001");
        entityManager.persist(subscription);

        payment = new Operation();
        payment.setAccount(account);
        payment.setSubscription(subscription);
        payment.setOpType(OperationType.PAYMENT);
        payment.setOpTime(Timestamp.from(Instant.parse("2024-02-01T10:00:00Z")));
        payment.setAmount(new BigDecimal("100.00"));
        payment.setDescription("Payment");
        entityManager.persist(payment);

        charge = new Operation();
        charge.setAccount(account);
        charge.setSubscription(subscription);
        charge.setOpType(OperationType.CHARGE);
        charge.setOpTime(Timestamp.from(Instant.parse("2024-02-10T10:00:00Z")));
        charge.setAmount(new BigDecimal("30.00"));
        charge.setDescription("Charge");
        entityManager.persist(charge);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getById_shouldReturnOperation_whenExists() {
        Operation result = operationRepository.getById(payment.getId());

        assertNotNull(result);
        assertEquals(payment.getId(), result.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotExists() {
        assertNull(operationRepository.getById(-1L));
    }

    @Test
    void getAll_shouldReturnAllOperations() {
        Collection<Operation> result = operationRepository.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void save_shouldPersistOperation() {
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setSubscription(subscription);
        operation.setOpType(OperationType.CHARGE);
        operation.setOpTime(Timestamp.from(Instant.parse("2024-03-01T10:00:00Z")));
        operation.setAmount(new BigDecimal("20.00"));
        operation.setDescription("New operation");

        operationRepository.save(operation);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(operation.getId());

        Operation persisted = entityManager.find(Operation.class, operation.getId());
        assertNotNull(persisted);
        assertEquals("New operation", persisted.getDescription());
    }

    @Test
    void update_shouldModifyOperation() {
        Operation operation = entityManager.find(Operation.class, payment.getId());
        operation.setDescription("Updated payment");

        operationRepository.update(operation);
        entityManager.flush();
        entityManager.clear();

        Operation updated = entityManager.find(Operation.class, payment.getId());
        assertEquals("Updated payment", updated.getDescription());
    }

    @Test
    void delete_shouldRemoveOperation() {
        Operation operation = entityManager.find(Operation.class, charge.getId());

        operationRepository.delete(operation);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Operation.class, charge.getId()));
    }

    @Test
    void findByAccountId_shouldReturnMatchingOperations() {
        Collection<Operation> result = operationRepository.findByAccountId(account.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findByAccountIdAndPeriod_shouldReturnMatchingOperations() {
        Collection<Operation> result = operationRepository.findByAccountIdAndPeriod(
                account.getId(),
                Timestamp.from(Instant.parse("2024-02-01T00:00:00Z")),
                Timestamp.from(Instant.parse("2024-02-05T00:00:00Z"))
        );

        assertEquals(1, result.size());
        assertEquals(OperationType.PAYMENT, result.iterator().next().getOpType());
    }

    @Test
    void findByAccountIdAndType_shouldReturnMatchingType() {
        Collection<Operation> result = operationRepository.findByAccountIdAndType(
                account.getId(),
                OperationType.CHARGE
        );

        assertEquals(1, result.size());
        assertEquals(OperationType.CHARGE, result.iterator().next().getOpType());
    }

    @Test
    void getNetAmountByAccountIdAndPeriod_shouldCalculateNetAmount() {
        BigDecimal result = operationRepository.getNetAmountByAccountIdAndPeriod(
                account.getId(),
                Timestamp.from(Instant.parse("2024-02-01T00:00:00Z")),
                Timestamp.from(Instant.parse("2024-02-28T23:59:59Z"))
        );

        assertEquals(0, new BigDecimal("70.00").compareTo(result));
    }

    @Test
    void getNetAmountByAccountIdAndPeriod_shouldReturnZero_whenNoOperations() {
        BigDecimal result = operationRepository.getNetAmountByAccountIdAndPeriod(
                account.getId(),
                Timestamp.from(Instant.parse("2025-01-01T00:00:00Z")),
                Timestamp.from(Instant.parse("2025-01-31T23:59:59Z"))
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void findBySubscriptionId_shouldReturnMatchingOperations() {
        Collection<Operation> result = operationRepository.findBySubscriptionId(subscription.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findBySubscriptionIdAndPeriod_shouldReturnMatchingOperations() {
        Collection<Operation> result = operationRepository.findBySubscriptionIdAndPeriod(
                subscription.getId(),
                Timestamp.from(Instant.parse("2024-02-09T00:00:00Z")),
                Timestamp.from(Instant.parse("2024-02-11T00:00:00Z"))
        );

        assertEquals(1, result.size());
        assertEquals(OperationType.CHARGE, result.iterator().next().getOpType());
    }

    @Test
    void findLatestByAccountId_shouldReturnLatestOperation() {
        Operation result = operationRepository.findLatestByAccountId(account.getId());

        assertNotNull(result);
        assertEquals(charge.getId(), result.getId());
    }

    @Test
    void findLatestByAccountId_shouldReturnNull_whenNoOperations() {
        Client client = new Client();
        client.setName("Empty Ops");
        client.setClientType(ClientType.PERSON);

        Account emptyAccount = new Account();
        emptyAccount.setClient(client);
        emptyAccount.setBalance(new BigDecimal("0.00"));
        emptyAccount.setCreditLimit(new BigDecimal("0.00"));
        client.setAccount(emptyAccount);

        entityManager.persist(client);
        entityManager.persist(emptyAccount);
        entityManager.flush();
        entityManager.clear();

        assertNull(operationRepository.findLatestByAccountId(emptyAccount.getId()));
    }


}
