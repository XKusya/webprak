INSERT INTO ServiceType (name)
VALUES
  ('HOME_INTERNET'),
  ('PHONE_NUMBER'),
  ('MOBILE_INTERNET');

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'Mobile number plan',
       'Basic mobile phone service',
       '{"unit":"PER_MIN","basePrice":0.10}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'PHONE_NUMBER';

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'Home internet 100Mbps',
       'Unlimited home internet connection (100 Mbps)',
       '{"unit":"PER_MONTH","basePrice":20.00,"speedMbps":100}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'HOME_INTERNET';

INSERT INTO Service (service_type_id, name, description, billing, is_active)
SELECT st.id,
       'Home internet 1000Mbps',
       'Unlimited home internet connection (1000 Mbps)',
       '{"unit":"PER_MONTH","basePrice":35.00,"speedMbps":1000}'::jsonb,
       TRUE
FROM ServiceType st
WHERE st.name = 'HOME_INTERNET';

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
  'Person',
  '{"passport":"1234 567890","birthDate":"1995-04-12"}'::jsonb,
  '{"phones":["+79990000001"],"emails":["ivan@example.com"]}'::jsonb,
  NOW()
;

INSERT INTO Client (name, client_type, details, contacts, created_at)
SELECT
  'Tech Solutions LLC',
  'Organization',
  '{"inn":"7701234567","ogrn":"1027700132195"}'::jsonb,
  '{"phones":["+74951234567"],"emails":["info@techsolutions.ru"]}'::jsonb,
  NOW();

INSERT INTO Client (name, client_type, details, contacts, created_at)
SELECT
  'Anna Smirnova',
  'Person',
  '{"passport":"4321 009988","birthDate":"2001-09-03"}'::jsonb,
  '{"phones":["+79990000002"],"emails":["anna@example.com"]}'::jsonb,
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
       'Active',
       '79990000001',
       '{"tariff":"standard"}'::jsonb
FROM Client c
JOIN Service s ON s.name = 'Mobile number plan'
WHERE c.name = 'Ivan Petrov';

INSERT INTO ClientService (client_id, service_id, started_at, ended_at, status, external_id, params)
SELECT c.id,
       s.id,
       NOW() - INTERVAL '60 days',
       NULL,
       'Active',
       'contract-001',
       '{"staticIp":true}'::jsonb
FROM Client c
JOIN Service s ON s.name = 'Home internet 100Mbps'
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO ClientService (client_id, service_id, started_at, ended_at, status, external_id, params)
SELECT c.id,
       s.id,
       NOW() - INTERVAL '10 days',
       NULL,
       'Active',
       '79990000002',
       '{"quotaGb":20,"tariff":"mobile-20gb"}'::jsonb
FROM Client c
JOIN Service s ON s.name = 'Mobile internet 20GB'
WHERE c.name = 'Anna Smirnova';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'Payment',
       NOW() - INTERVAL '25 days',
       200.00,
       NULL,
       'Initial deposit'
FROM Account a
JOIN Client c ON c.id = a.client_id
WHERE c.name = 'Ivan Petrov';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'Charge',
       NOW() - INTERVAL '5 days',
       15.00,
       cs.id,
       'Mobile usage charges'
FROM Client c
JOIN Account a ON a.client_id = c.id
JOIN Service s ON s.name = 'Mobile number plan'
JOIN ClientService cs ON cs.client_id = c.id AND cs.service_id = s.id AND cs.status = 'Active'
WHERE c.name = 'Ivan Petrov';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'Payment',
       NOW() - INTERVAL '50 days',
       1000.00,
       NULL,
       'Contract prepayment'
FROM Account a
JOIN Client c ON c.id = a.client_id
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'Charge',
       NOW() - INTERVAL '3 days',
       20.00,
       cs.id,
       'Monthly internet fee'
FROM Client c
JOIN Account a ON a.client_id = c.id
JOIN Service s ON s.name = 'Home internet 100Mbps'
JOIN ClientService cs ON cs.client_id = c.id AND cs.service_id = s.id AND cs.status = 'Active'
WHERE c.name = 'Tech Solutions LLC';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'Payment',
       NOW() - INTERVAL '9 days',
       100.00,
       NULL,
       'Top-up for mobile internet'
FROM Account a
JOIN Client c ON c.id = a.client_id
WHERE c.name = 'Anna Smirnova';

INSERT INTO Operation (account_id, op_type, op_time, amount, client_service_id, description)
SELECT a.id,
       'Charge',
       NOW() - INTERVAL '2 days',
       12.00,
       cs.id,
       'Monthly mobile internet fee'
FROM Client c
JOIN Account a ON a.client_id = c.id
JOIN Service s ON s.name = 'Mobile internet 20GB'
JOIN ClientService cs ON cs.client_id = c.id AND cs.service_id = s.id AND cs.status = 'Active'
WHERE c.name = 'Anna Smirnova';