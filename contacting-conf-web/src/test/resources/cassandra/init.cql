CREATE KEYSPACE IF NOT EXISTS contacting WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS contacting.contacting_configs (
	key text,
	values text,
	PRIMARY KEY (key)
);

CREATE TABLE IF NOT EXISTS contacting.survey_call_records (account_friendly_name VARCHAR ,
                                                           phone_num VARCHAR ,
                                                           survey_name VARCHAR ,
                                                           offered_datetime TIMESTAMP ,
                                                           PRIMARY KEY ((account_friendly_name, phone_num), offered_datetime))
                                                           WITH CLUSTERING ORDER BY (offered_datetime DESC);

INSERT INTO contacting.survey_call_records(account_friendly_name,phone_num, survey_name, offered_datetime) VALUES ('NL-dev', '+31687855555', 'nlsurveyitest1', 1642064153015);
INSERT INTO contacting.survey_call_records(account_friendly_name,phone_num, survey_name, offered_datetime) VALUES ('NL-dev', '+31687855556', 'besurveyitest1', 1542064183015);
INSERT INTO contacting.survey_call_records(account_friendly_name,phone_num, survey_name, offered_datetime) VALUES ('NL-dev', '+31687855555', 'nlsurveyitest2', 1642064153016);
INSERT INTO contacting.survey_call_records(account_friendly_name,phone_num, survey_name, offered_datetime) VALUES ('NL-dev', '+31687855556', 'besurveyitest2', 1542064183016);

CREATE TABLE IF NOT EXISTS contacting.business_functions (account_friendly_name text,
                                                          business_function text,
                                                          role text,
                                                          restriction text,
                                                          PRIMARY KEY (account_friendly_name, business_function, role));

CREATE TABLE IF NOT EXISTS contacting.business_functions_on_teams (account_friendly_name text,
                                                                   business_function text,
                                                                   organisation_id int,
                                                                   role text,
                                                                   restriction text,
                                                                   PRIMARY KEY (account_friendly_name, business_function,organisation_id, role));

CREATE TYPE IF NOT EXISTS contacting.organisational_restriction(clt_id int,clt_name text,circle_id int, circle_name text,super_circle_id int,super_circle_name text, preferred boolean);

CREATE TABLE IF NOT EXISTS contacting.employees_by_account (
                                                               employee_id VARCHAR,
                                                               account_friendly_name VARCHAR,
                                                               preferred_account boolean,
                                                               business_unit text,
                                                               department text,
                                                               team text,
                                                               roles varchar,
                                                               organisational_restrictions SET<FROZEN<organisational_restriction>>,
                                                               allowed_channels MAP<text, int>,
                                                               worker_sid text,
                                                               PRIMARY KEY (employee_id, account_friendly_name)
    );
