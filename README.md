Биллинговая база оператора связи
=
## Описание веб-сайта и его страниц
В этом проекте можно выделить основные 3 сущности: **Клиент**, **услуга**, **операция**. 

Предлагается разделить сайт на 4 страницы:
- Главная страница, которая агрегирует другие страницы. На ней так же будет справка.
- Страница для работы со списком клиентов.
- Страница для работы со списком услуг.
- Страница для работы с операциями.

Ниже прикреплена схема сайта с действиями, которые можно на нём выполнить:

```mermaid
---
config:
    theme: dark
    layout: elk
---
flowchart TD

    A["Главная страница"]

    B["Клиенты"]
    C["Операции"]
    D["Услуги"]
    
    E("Зарегистрировать клиента" )
    F("Просмотреть карточку <br/>клиента")
    G("Редактировать карточку<br/>клиента")

    H("Регистрировать поступление/списание")

    I("Просмотреть информацию<br/>об услуге")
    J("Добавить услугу")
    K("Редактировать информацию<br/>об услуге")

    %% Главная навигация
    A --> B
    A --> C
    A --> D

    %% Клиенты
    B --> E
    B --> F
    F --> G

    %% Операции
    C --> H
    H --> F
    C --> I

    %% Услуги
    D --> I
    D --> J
    I --> K

    style E fill: gray
    style F fill: gray
    style G fill: gray
    style H fill: gray
    style I fill: gray
    style J fill: gray
    style K fill: gray
```

## Схема базы данных
```mermaid
---
config: 
    theme: dark
    layout: elk
---
erDiagram
    Client {
        bigserial id PK
        varchar name
        varchar clientType "PERSON|ORG"
        jsonb details
        jsonb contacts
        timestamp createdAt
    }

    Account {
        bigserial id PK
        bigint clientId FK
        numeric balance
        numeric creditLimit
        date debtDueDate
    }

    ServiceType {
        bigserial id PK
        varchar name
    }

    Service {
        bigserial id PK
        bigint serviceTypeId FK
        varchar name
        text description
        jsonb billing
        boolean isActive
    }

    ClientService {
        bigserial id PK
        bigint clientId FK
        bigint serviceId FK
        timestamp startedAt
        timestamp endedAt
        varchar status "ACTIVE|ENDED"
        varchar externalId
        jsonb params
    }

    Operation {
        bigserial id PK
        bigint accountId FK
        varchar opType "PAYMENT|CHARGE"
        timestamp opTime
        numeric amount
        bigint clientServiceId FK
        text description
    }
        
    Client ||--|| Account : "Account.clientId -> Client.id"
    ServiceType ||--o{ Service : "ServiceType.id -> Service.serviceTypeId"
    Client ||--o{ ClientService : "ClientService.clientId -> Client.id"
    Service ||--o{ ClientService : "ClientService.serviceId -> Service.id"
    Account ||--o{ Operation : "Operation.accountId -> Account.id"
    ClientService ||--o{ Operation : "Operation.clientServiceId -> ClientService.id"
```

# TODO
- [x] Сценарии использования
- [x] Схема навигации по сайту
- [x] Схема базы данных
- [x] Сделать скелет проекта 
- [x] Добавить скрипт инициализации БД
- [x] Добавить задачи создания, заполнения, отчистки и вывода содержимого бд.
- [ ] Back-End
- [ ] Front-End