package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.models.client.Client;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;

public interface ClientRepository extends BaseRepository<Client, Long> {

    Collection<Client> findByNameContaining(String namePart);

    Collection<Client> findByServiceAndPeriod(Long serviceId, Timestamp from, Timestamp to);

    Collection<Client> findWithNegativeBalance();
    Collection<Client> findWithCreditLimitExceeded();
    Collection<Client> findWithOverdueDebt(Date currentDate);

    Client findDetailedById(Long id);

    boolean canBeDeleted(Long clientId);
}