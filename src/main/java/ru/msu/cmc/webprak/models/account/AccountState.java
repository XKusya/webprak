package ru.msu.cmc.webprak.models.account;

import java.math.BigDecimal;
import java.time.LocalDate;

public enum AccountState {
    ALL("Все"),
    NEGATIVE_BALANCE("Отрицательный баланс"),
    CREDIT_LIMIT_EXCEEDED("Превышение кредитного лимита"),
    OVERDUE_DEBT("Просроченная задолженность");

    private final String label;

    AccountState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(Account account) {
        if (account == null) {
            return false;
        }
        BigDecimal balance = account.getBalance();
        BigDecimal creditLimit = account.getCreditLimit();
        LocalDate debtDueDate = account.getDebtDueDate();
        return switch (this) {
            case NEGATIVE_BALANCE -> balance != null && balance.compareTo(BigDecimal.ZERO) < 0;
            case CREDIT_LIMIT_EXCEEDED ->
                    balance != null && creditLimit != null && balance.compareTo(creditLimit.negate()) < 0;
            case OVERDUE_DEBT -> debtDueDate != null && debtDueDate.isBefore(LocalDate.now());
            case ALL -> true;
        };
    }
}