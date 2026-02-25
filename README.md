Биллинговая база оператора связи
=
## Описание веб-сайта и его страниц
В этом проекте можно выделить основные 3 сущности: **Клиент**, **услуга**, **операция**. 

Предлагается разделить сайт на 4 основные страницы:
- Главная страница, которая агрегирует другие страницы. На ней так же будет справка.
- Страница для работы со списком клиентов.
- Страница для работы со списком услуг.
- Страница для работы с операциями.

И 7 вспомогательных, на которых можно выполнить следующие действия:
- Страница регистрации клиента
- Карточка клиента
- Редактирование карточки клиента
- Регистрация поступления/списания
- Информация об услуге
- Редактирование услуги
- Добавление услуги

Ниже прикреплена схема сайта с действиями, которые можно на нём выполнить:
```mermaid
flowchart TD
    M["/ <br> Главная страница"]
    C["/clients <br> Клиенты"]
    O["/operations <br> Операции"]
    S["/services <br> Услуги"]
    CN["/clients/new <br> Зарегистрировать клиента"]
    CI["/clients/{id} <br> Просмотреть карточку клиента"]
    CIE["/clients/{id}/edit <br> Редактировать карточку клиента"]
    ON["/operations/new <br> Регистрировать поступление/списание"]
    SI["/services/{id} <br> Просмотреть информацию об услуге"]
    SN["/services/new <br> Добавить услугу"]
    SIE["/services/{id}/edit <br> Редактировать информацию об услуге"]
    M --> C
    M --> O
    M --> S

    C --> CN
    C --> CI
    CI --> CIE

    O --> ON
    O --> CI
    O --> SI

    S --> SI
    S --> SN
    SI --> SIE
    style CN fill: gray
    style CI fill: gray
    style CIE fill: gray
    style ON fill: gray
    style SI fill: gray
    style SN fill: gray
    style SIE fill: gray
```
Более подробно ознакомится со сценариями использования можно [здесь](./docs/usercases.md). 
Более подробно ознакомится со схемой сайта можно [здесь](./docs/site-struct.md).

## Схема базы данных
```mermaid
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

    Client ||--|| Account: "Account.clientId -> Client.id"
    ServiceType ||--o{ Service: "ServiceType.id -> Service.serviceTypeId"
    Client ||--o{ ClientService: "ClientService.clientId -> Client.id"
    Service ||--o{ ClientService: "ClientService.serviceId -> Service.id"
    Account ||--o{ Operation: "Operation.accountId -> Account.id"
    ClientService ||--o{ Operation: "Operation.clientServiceId -> ClientService.id"
```
Описания таблиц и JSON-схем, используемых в таблицах можно посмотреть [здесь](./docs/database.md).

# TODO
- [x] Сценарии использования
- [x] Схема навигации по сайту
- [x] Схема базы данных
- [x] Сделать скелет проекта 
- [x] Добавить скрипт инициализации БД
- [x] Добавить задачи создания, заполнения, отчистки и вывода содержимого бд.
- [ ] Back-End
- [ ] Front-End