INSERT INTO ServiceType (name)
VALUES
  ('MOBILE_VOICE'),
  ('MOBILE_INTERNET'),
  ('SMS');

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'Mobile voice plan',
       'Basic mobile voice service',
       '{"unit":"PER_MIN","basePrice":0.10}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'MOBILE_VOICE';

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'SMS basic',
       'Standard SMS service',
       '{"unit":"PER_SMS","basePrice":0.05}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'SMS';

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'SMS bundle 100',
       'SMS package with 100 messages',
       '{"unit":"PER_SMS","basePrice":0.04,"bundleSize":100,"bundlePrice":4.00}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'SMS';

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'Mobile internet 20GB',
       'Monthly mobile internet package',
       '{"unit":"PER_MONTH","basePrice":12.00,"quotaGb":20}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'MOBILE_INTERNET';

INSERT INTO Client (name, client_type, details, contacts, created_at)
SELECT
  'Ivan Petrov',
  'PERSON',
  '{"passport":"1234 567890","birthDate":"1995-04-12"}'::jsonb,
  '[{"type":"PHONE","value":"+79990000001"},{"type":"EMAIL","value":"ivan@example.com"}]'::jsonb,
  NOW();

INSERT INTO Client (name, client_type, details, contacts, created_at)
SELECT
  'Tech Solutions LLC',
  'ORG',
  '{"inn":"7701234567","ogrn":"1027700132195"}'::jsonb,
  '[{"type":"PHONE","value":"+74951234567"},{"type":"EMAIL","value":"info@techsolutions.ru"}]'::jsonb,
  NOW();

INSERT INTO Client (name, client_type, details, contacts, created_at)
SELECT
  'Anna Smirnova',
  'PERSON',
  '{"passport":"4321 009988","birthDate":"2001-09-03"}'::jsonb,
  '[{"type":"PHONE","value":"+79990000002"},{"type":"EMAIL","value":"anna@example.com"}]'::jsonb,
  NOW();

INSERT INTO Account (client_id, balance, credit_limit, debt_due_date)
SELECT c.id,
       100.00,
       50.00,
       CURRENT_DATE + INTERVAL '30 days'
FROM Client c
WHERE c.name = 'Ivan Petrov';

INSERT INTO Account (client_id, balance, credit_limit, debt_due_date)
SELECT c.id,
       500.00,
       200.00,
       CURRENT_DATE + INTERVAL '30 days'
FROM Client c
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO Account (client_id, balance, credit_limit, debt_due_date)
SELECT c.id,
       60.00,
       30.00,
       CURRENT_DATE + INTERVAL '30 days'
FROM Client c
WHERE c.name = 'Anna Smirnova';

INSERT INTO ClientService (client_id, service_id, started_at, ended_at, status, external_id, params)
SELECT c.id,
       s.id,
       NOW() - INTERVAL '30 days',
       NULL,
       'ACTIVE',
       '79990000001',
       '{"tariff":"standard"}'::jsonb
FROM Client c
JOIN Service s ON s.name = 'Mobile voice plan'
WHERE c.name = 'Ivan Petrov';

INSERT INTO ClientService (client_id, service_id, started_at, ended_at, status, external_id, params)
SELECT c.id,
       s.id,
       NOW() - INTERVAL '60 days',
       NULL,
       'ACTIVE',
       'contract-001',
       '{"tariff":"standard"}'::jsonb
FROM Client c
JOIN Service s ON s.name = 'SMS basic'
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO ClientService (client_id, service_id, started_at, ended_at, status, external_id, params)
SELECT c.id,
       s.id,
       NOW() - INTERVAL '10 days',
       NULL,
       'ACTIVE',
       '79990000002',
       '{"quotaGb":20,"tariff":"mobile-20gb"}'::jsonb
FROM Client c
JOIN Service s ON s.name = 'Mobile internet 20GB'
WHERE c.name = 'Anna Smirnova';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'PAYMENT',
       NOW() - INTERVAL '25 days',
       200.00,
       NULL,
       'Initial deposit'
FROM Account a
JOIN Client c ON c.id = a.client_id
WHERE c.name = 'Ivan Petrov';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'CHARGE',
       NOW() - INTERVAL '5 days',
       15.00,
       cs.id,
       'Mobile usage charges'
FROM Client c
JOIN Account a ON a.client_id = c.id
JOIN Service s ON s.name = 'Mobile voice plan'
JOIN ClientService cs ON cs.client_id = c.id AND cs.service_id = s.id AND cs.status = 'ACTIVE'
WHERE c.name = 'Ivan Petrov';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'PAYMENT',
       NOW() - INTERVAL '50 days',
       1000.00,
       NULL,
       'Contract prepayment'
FROM Account a
JOIN Client c ON c.id = a.client_id
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'CHARGE',
       NOW() - INTERVAL '3 days',
       10.00,
       cs.id,
       'Monthly SMS fee'
FROM Client c
JOIN Account a ON a.client_id = c.id
JOIN Service s ON s.name = 'SMS basic'
JOIN ClientService cs ON cs.client_id = c.id AND cs.service_id = s.id AND cs.status = 'ACTIVE'
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'PAYMENT',
       NOW() - INTERVAL '9 days',
       100.00,
       NULL,
       'Top-up for mobile internet'
FROM Account a
JOIN Client c ON c.id = a.client_id
WHERE c.name = 'Anna Smirnova';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'CHARGE',
       NOW() - INTERVAL '2 days',
       12.00,
       cs.id,
       'Monthly mobile internet fee'
FROM Client c
JOIN Account a ON a.client_id = c.id
JOIN Service s ON s.name = 'Mobile internet 20GB'
JOIN ClientService cs ON cs.client_id = c.id AND cs.service_id = s.id AND cs.status = 'ACTIVE'
WHERE c.name = 'Anna Smirnova';
