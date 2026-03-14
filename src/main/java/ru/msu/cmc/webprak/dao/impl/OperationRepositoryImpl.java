package ru.msu.cmc.webprak.dao.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprak.dao.OperationRepository;
import ru.msu.cmc.webprak.models.operation.Operation;
import ru.msu.cmc.webprak.models.operation.OperationType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public class OperationRepositoryImpl extends BaseRepositoryImpl<Operation, Long> implements OperationRepository {

    public OperationRepositoryImpl() {
        super(Operation.class);
    }

    @Override
    public Collection<Operation> findByAccountId(Long accountId) {
        return entityManager
                .createQuery(
                        "SELECT o FROM Operation o " +
                                "WHERE o.account.id = :accountId " +
                                "ORDER BY o.opTime DESC, o.id DESC",
                        Operation.class
                )
                .setParameter("accountId", accountId)
                .getResultList();
    }

    @Override
    public Collection<Operation> findByAccountIdAndPeriod(
            Long accountId,
            Timestamp from,
            Timestamp to
    ) {
        return entityManager
                .createQuery(
                        "SELECT o FROM Operation o " +
                                "WHERE o.account.id = :accountId " +
                                "AND o.opTime BETWEEN :from AND :to " +
                                "ORDER BY o.opTime DESC, o.id DESC",
                        Operation.class
                )
                .setParameter("accountId", accountId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    @Override
    public Collection<Operation> findByAccountIdAndType(Long accountId, OperationType operationType) {
        return entityManager
                .createQuery(
                        "SELECT o FROM Operation o " +
                                "WHERE o.account.id = :accountId " +
                                "AND o.opType = :operationType " +
                                "ORDER BY o.opTime DESC, o.id DESC",
                        Operation.class
                )
                .setParameter("accountId", accountId)
                .setParameter("operationType", operationType)
                .getResultList();
    }

    @Override
    public BigDecimal getNetAmountByAccountIdAndPeriod(
            Long accountId,
            Timestamp from,
            Timestamp to
    ) {
        BigDecimal result = entityManager
                .createQuery(
                        "SELECT COALESCE(SUM(" +
                                "CASE " +
                                "WHEN o.opType = :paymentType THEN o.amount " +
                                "WHEN o.opType = :chargeType THEN -o.amount " +
                                "ELSE 0 " +
                                "END), 0) " +
                                "FROM Operation o " +
                                "WHERE o.account.id = :accountId " +
                                "AND o.opTime BETWEEN :from AND :to",
                        BigDecimal.class
                )
                .setParameter("paymentType", OperationType.PAYMENT)
                .setParameter("chargeType", OperationType.CHARGE)
                .setParameter("accountId", accountId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public Collection<Operation> findBySubscriptionId(Long subscriptionId) {
        return entityManager
                .createQuery(
                        "SELECT o FROM Operation o " +
                                "WHERE o.subscription.id = :subscriptionId " +
                                "ORDER BY o.opTime DESC, o.id DESC",
                        Operation.class
                )
                .setParameter("subscriptionId", subscriptionId)
                .getResultList();
    }

    @Override
    public Collection<Operation> findBySubscriptionIdAndPeriod(
            Long subscriptionId,
            Timestamp from,
            Timestamp to
    ) {
        return entityManager
                .createQuery(
                        "SELECT o FROM Operation o " +
                                "WHERE o.subscription.id = :subscriptionId " +
                                "AND o.opTime BETWEEN :from AND :to " +
                                "ORDER BY o.opTime DESC, o.id DESC",
                        Operation.class
                )
                .setParameter("subscriptionId", subscriptionId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    @Override
    public Operation findLatestByAccountId(Long accountId) {
        List<Operation> result = entityManager
                .createQuery(
                        "SELECT o FROM Operation o " +
                                "WHERE o.account.id = :accountId " +
                                "ORDER BY o.opTime DESC, o.id DESC",
                        Operation.class
                )
                .setParameter("accountId", accountId)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }
}