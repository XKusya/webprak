package ru.msu.cmc.webprak.models.service;

public enum ServiceStatus {
    ALL("Все"),
    ACTIVE("Активные"),
    INACTIVE("Неактивные");

    private final String label;

    ServiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(Service service) {
        if (service == null || service.getIsActive() == null) {
            return this == ALL;
        }
        return switch (this) {
            case ACTIVE -> Boolean.TRUE.equals(service.getIsActive());
            case INACTIVE -> Boolean.FALSE.equals(service.getIsActive());
            case ALL -> true;
        };
    }
}
