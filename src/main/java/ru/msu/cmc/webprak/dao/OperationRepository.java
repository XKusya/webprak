package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.models.operation.Operation;
import ru.msu.cmc.webprak.models.operation.OperationType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

public interface OperationRepository extends BaseRepository<Operation, Long> {
    Collection<Operation> findByAccountId(Long accountId);
    Collection<Operation> findByAccountIdAndPeriod(Long accountId, Timestamp from, Timestamp to);
    Collection<Operation> findByAccountIdAndType(Long accountId, OperationType operationType);
    BigDecimal getNetAmountByAccountIdAndPeriod(Long accountId, Timestamp from, Timestamp to);

    Collection<Operation> findBySubscriptionId(Long subscriptionId);
    Collection<Operation> findBySubscriptionIdAndPeriod(Long subscriptionId, Timestamp from, Timestamp to);

    Operation findLatestByAccountId(Long accountId);
}