package ru.msu.cmc.webprak.dao.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.ServiceRepository;
import ru.msu.cmc.webprak.models.service.Service;
import ru.msu.cmc.webprak.models.servicetype.ServiceType;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceRepositoryImplTest {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EntityManager entityManager;

    private ServiceType mobileInternetType;
    private ServiceType smsType;

    private Service activeService1;
    private Service activeService2;
    private Service inactiveService;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Operation").executeUpdate();
        entityManager.createQuery("DELETE FROM Subscription").executeUpdate();
        entityManager.createQuery("DELETE FROM Service").executeUpdate();
        entityManager.createQuery("DELETE FROM ServiceType").executeUpdate();

        mobileInternetType = new ServiceType();
        mobileInternetType.setName("MOBILE_INTERNET");
        entityManager.persist(mobileInternetType);

        smsType = new ServiceType();
        smsType.setName("SMS");
        entityManager.persist(smsType);

        activeService1 = new Service();
        activeService1.setServiceType(mobileInternetType);
        activeService1.setName("Mobile Internet 20GB");
        activeService1.setDescription("Internet package");
        activeService1.setIsActive(true);
        entityManager.persist(activeService1);

        activeService2 = new Service();
        activeService2.setServiceType(smsType);
        activeService2.setName("SMS Pack");
        activeService2.setDescription("SMS package");
        activeService2.setIsActive(true);
        entityManager.persist(activeService2);

        inactiveService = new Service();
        inactiveService.setServiceType(mobileInternetType);
        inactiveService.setName("Archive Internet");
        inactiveService.setDescription("Old tariff");
        inactiveService.setIsActive(false);
        entityManager.persist(inactiveService);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getById_shouldReturnService_whenExists() {
        Service result = serviceRepository.getById(activeService1.getId());

        assertNotNull(result);
        assertEquals(activeService1.getId(), result.getId());
        assertEquals("Mobile Internet 20GB", result.getName());
    }

    @Test
    void getById_shouldReturnNull_whenNotExists() {
        Service result = serviceRepository.getById(-1L);

        assertNull(result);
    }

    @Test
    void getAll_shouldReturnAllServices() {
        Collection<Service> result = serviceRepository.getAll();

        assertEquals(3, result.size());
    }

    @Test
    void save_shouldPersistEntity() {
        Service newService = new Service();
        newService.setServiceType(smsType);
        newService.setName("New SMS Plan");
        newService.setDescription("New");
        newService.setIsActive(true);

        serviceRepository.save(newService);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(newService.getId());

        Service persisted = entityManager.find(Service.class, newService.getId());
        assertNotNull(persisted);
        assertEquals("New SMS Plan", persisted.getName());
    }

    @Test
    void update_shouldChangeEntity() {
        Service service = entityManager.find(Service.class, activeService1.getId());
        service.setDescription("Updated description");
        service.setIsActive(false);

        serviceRepository.update(service);
        entityManager.flush();
        entityManager.clear();

        Service updated = entityManager.find(Service.class, activeService1.getId());
        assertEquals("Updated description", updated.getDescription());
        assertFalse(updated.getIsActive());
    }

    @Test
    void delete_shouldRemoveEntity() {
        Service service = entityManager.find(Service.class, inactiveService.getId());

        serviceRepository.delete(service);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Service.class, inactiveService.getId()));
    }

    @Test
    void findActive_shouldReturnOnlyActive() {
        Collection<Service> result = serviceRepository.findActive();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Service::getIsActive));
    }

    @Test
    void findInactive_shouldReturnOnlyInactive() {
        Collection<Service> result = serviceRepository.findInactive();

        assertEquals(1, result.size());
        Service service = result.iterator().next();
        assertEquals("Archive Internet", service.getName());
        assertFalse(service.getIsActive());
    }

    @Test
    void findByNameContaining_shouldReturnMatches() {
        Collection<Service> result = serviceRepository.findByNameContaining("internet");

        assertEquals(2, result.size());
    }

    @Test
    void findByNameContaining_shouldReturnEmpty_whenNoMatches() {
        Collection<Service> result = serviceRepository.findByNameContaining("voice");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByServiceTypeId_shouldReturnMatches() {
        Collection<Service> result = serviceRepository.findByServiceTypeId(mobileInternetType.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getServiceType().getId().equals(mobileInternetType.getId())));
    }

    @Test
    void existsActiveByName_shouldReturnTrue_whenActiveExists() {
        assertTrue(serviceRepository.existsActiveByName("Mobile Internet 20GB"));
    }

    @Test
    void existsActiveByName_shouldReturnFalse_whenOnlyInactiveExists() {
        assertFalse(serviceRepository.existsActiveByName("Archive Internet"));
    }

    @Test
    void existsActiveByName_shouldReturnFalse_whenNoSuchService() {
        assertFalse(serviceRepository.existsActiveByName("Unknown"));
    }
}
