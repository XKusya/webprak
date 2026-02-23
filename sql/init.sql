CREATE TYPE ClientType AS ENUM ('Person', 'Organization');
CREATE TYPE ClientServiceStatus AS ENUM ('Active', 'Ended');
CREATE TYPE OperationType AS ENUM ('Payment', 'Charge');

CREATE TABLE Client (
    id BIGSERIAL NOT NULL,
    name VARCHAR(100),
    client_type ClientType,
    details jsonb,
    contacts jsonb,
    created_at timestamp,
    CONSTRAINT pk_client PRIMARY KEY (id)
);

CREATE TABLE Account (
     id BIGSERIAL NOT NULL,
     client_id BIGINT NOT NULL,
     balance NUMERIC,
     credit_limit NUMERIC,
     debt_due_date DATE,
     CONSTRAINT pk_account PRIMARY KEY (id),
     CONSTRAINT uq_account_client UNIQUE (client_id),
     CONSTRAINT fk_account_client FOREIGN KEY (client_id)
         REFERENCES Client(id)
         ON DELETE CASCADE
);

CREATE TABLE ServiceType (
     id BIGSERIAL NOT NULL,
     name VARCHAR(100) NOT NULL,
     CONSTRAINT pk_service_type PRIMARY KEY (id),
     CONSTRAINT uq_service_type_name UNIQUE (name)
);

CREATE TABLE Service (
     id BIGSERIAL NOT NULL,
     service_type_id BIGINT NOT NULL,
     name VARCHAR(100) NOT NULL,
     description TEXT,
     billing jsonb,
     is_active BOOLEAN NOT NULL DEFAULT TRUE,
     CONSTRAINT pk_service PRIMARY KEY (id),
     CONSTRAINT fk_service_service_type FOREIGN KEY (service_type_id)
         REFERENCES ServiceType(id)
);

CREATE TABLE ClientService (
   id BIGSERIAL NOT NULL,
   client_id BIGINT NOT NULL,
   service_id BIGINT NOT NULL,
   started_at TIMESTAMP NOT NULL,
   ended_at TIMESTAMP,
   status ClientServiceStatus NOT NULL DEFAULT 'Active',
   external_id VARCHAR(100),
   params jsonb,
   CONSTRAINT pk_client_service PRIMARY KEY (id),
   CONSTRAINT fk_client_service_client FOREIGN KEY (client_id)
       REFERENCES Client(id)
       ON DELETE CASCADE,
   CONSTRAINT fk_client_service_service FOREIGN KEY (service_id)
       REFERENCES Service(id),
   CONSTRAINT ck_client_service_dates CHECK (
       ended_at IS NULL OR ended_at >= started_at
       )
);

CREATE TABLE Operation (
   id BIGSERIAL NOT NULL,
   account_id BIGINT NOT NULL,
   op_type OperationType NOT NULL,
   op_time TIMESTAMP NOT NULL DEFAULT NOW(),
   amount NUMERIC NOT NULL,
   client_service_id BIGINT,
   description TEXT,
   CONSTRAINT pk_operation PRIMARY KEY (id),
   CONSTRAINT fk_operation_account FOREIGN KEY (account_id)
       REFERENCES Account(id)
       ON DELETE CASCADE,
   CONSTRAINT fk_operation_client_service FOREIGN KEY (client_service_id)
       REFERENCES ClientService(id),
   CONSTRAINT ck_operation_amount_positive CHECK (amount > 0)
);