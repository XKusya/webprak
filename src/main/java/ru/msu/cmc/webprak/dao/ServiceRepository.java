package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.models.service.Service;

import java.util.Collection;

public interface ServiceRepository extends BaseRepository<Service, Long> {
    Collection<Service> findActive();
    Collection<Service> findInactive();
    Collection<Service> findByNameContaining(String namePart);
    Collection<Service> findByServiceTypeId(Long serviceTypeId);

    boolean existsActiveByName(String name);
}