package ru.msu.cmc.webprak.dao.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprak.dao.ServiceRepository;
import ru.msu.cmc.webprak.models.service.Service;
import ru.msu.cmc.webprak.models.servicetype.ServiceType;

import java.util.Collection;

@Repository
@Transactional
public class ServiceRepositoryImpl extends BaseRepositoryImpl<Service, Long> implements ServiceRepository {

    public ServiceRepositoryImpl() {
        super(Service.class);
    }

    @Override
    public Collection<Service> findActive() {
        return entityManager
                .createQuery(
                        "SELECT s FROM Service s " +
                                "WHERE s.isActive = true " +
                                "ORDER BY s.name ASC",
                        Service.class
                )
                .getResultList();
    }

    @Override
    public Collection<Service> findInactive() {
        return entityManager
                .createQuery(
                        "SELECT s FROM Service s " +
                                "WHERE s.isActive = false " +
                                "ORDER BY s.name ASC",
                        Service.class
                )
                .getResultList();
    }

    @Override
    public Collection<Service> findByNameContaining(String namePart) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Service s " +
                                "WHERE LOWER(s.name) LIKE LOWER(:namePart) " +
                                "ORDER BY s.name ASC",
                        Service.class
                )
                .setParameter("namePart", "%" + namePart + "%")
                .getResultList();
    }

    @Override
    public Collection<Service> findByServiceTypeId(Long serviceTypeId) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Service s " +
                                "WHERE s.serviceType.id = :serviceTypeId " +
                                "ORDER BY s.name ASC",
                        Service.class
                )
                .setParameter("serviceTypeId", serviceTypeId)
                .getResultList();
    }

    @Override
    public boolean existsActiveByName(String name) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(s) FROM Service s " +
                                "WHERE LOWER(s.name) = LOWER(:name) " +
                                "AND s.isActive = true",
                        Long.class
                )
                .setParameter("name", name)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Collection<ServiceType> getServiceTypes() {
        return entityManager
                .createQuery(
                        "SELECT st FROM ServiceType st ORDER BY st.name ASC",
                        ServiceType.class
                )
                .getResultList();
    }

    @Override
    public ServiceType getServiceTypeById(Long id) {
        return entityManager.find(ServiceType.class, id);
    }
}
