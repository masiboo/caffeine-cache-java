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


insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'account recording management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'account recording management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'attribute management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'blacklist management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'customer tooling for open' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'customer tooling for open' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'customer tooling for closed' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'customer tooling for closed' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'contact handling' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'contact handling' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'contact handling' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'login' , 'OPS_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'organization administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'organization administration' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'account settings management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'account settings management' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'opening hours management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'opening hours management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'opening hours management' , 'OPS_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'survey management' , 'CONFIG_MANAGER' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'task queue config activation' , 'CONFIG_MANAGER' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'permissions administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'real time dashboard' , 'MONITORING' , -1 ,'CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'real time dashboard' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'real time dashboard' , 'AGENT' , -1 ,'SUPER_CIRCLE');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'real time dashboard' , 'AGENT' , 28, 'TEAM');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'real time dashboard' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'user administration' , 'ADMIN' , -1 ,'SUPER_CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'user administration' , 'SUPERVISOR' , -1 ,'CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'user administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'user administration' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'user administration' , 'CJE' , -1 ,'SUPER_CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'user session management' , 'CJE' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'task routing management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'task routing management' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'system tooling' , 'OPS_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-itest', 'status management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'login' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'external access' , 'FOREIGN_API' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for open' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'customer tooling for closed' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'contact handling' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'user administration' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'real time dashboard' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'organization administration' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'permissions administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('FR-itest', 'task routing management' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'callflow manager' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'customer tooling for open' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'account recording management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'account recording management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'attribute management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'blacklist management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'customer tooling for open' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'customer tooling for open' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'customer tooling for closed' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'customer tooling for closed' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'contact handling' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'contact handling' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'contact handling' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'MONITORING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'AGENT' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'CUSTOMER_UNAUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'CUSTOMER_AUTHENTICATED' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'OPS_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'organization administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'organization administration' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'organization administration' , 'OPS_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'account settings management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'account settings management' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'account settings management' , 'OPS_ADMIN' , -1 ,'ACCOUNT');


insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'opening hours management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'opening hours management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'opening hours management' , 'OPS_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'survey management' , 'CONFIG_MANAGER' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'survey management' , 'OPS_ADMIN' , -1 ,'ACCOUNT');


insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'permissions administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'real time dashboard' , 'MONITORING' , -1 ,'CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'real time dashboard' , 'SUPERVISOR' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'real time dashboard' , 'AGENT' , -1 ,'SUPER_CIRCLE');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'real time dashboard' , 'AGENT' , 28, 'TEAM');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'real time dashboard' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'user administration' , 'ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'user administration' , 'SUPERVISOR' , -1 ,'CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'user administration' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'user administration' , 'CONTACTING' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'user administration' , 'CJE' , -1 ,'SUPER_CIRCLE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'user session management' , 'CJE' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'task routing management' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'task routing management' , 'CONTACTING' , -1 ,'ACCOUNT');

insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'system tooling' , 'OPS_ADMIN' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'status management' , 'ACCOUNT_ADMIN' , -1 ,'ACCOUNT');


insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('AGENT10', 'NL-dev', true, 'SC1','C2','CLT5','AGENT',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false)});
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('AGENT11', 'NL-dev', true, 'SC_1','C_2','CLT_5','AGENT',{(39, 'CLT_5',33,'C_2',31,'SC_1',true)});
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('AGENT12', 'NL-dev', true, 'SC1','C2','CLT5','AGENT',{(28, 'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false)});

insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('AGENT1', 'NL-dev', true, 'bu-1','dep-1','team-1','AGENT',{(1,'team-1',2,'dep-1',3,'bu-1',true)});
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('AGENT2', 'NL-dev',true,'bu-1','dep-1','team-1','AGENT');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('AGENT3', 'NL-dev',true,'bu-1','dep-2','team-1','AGENT');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('AGENT4', 'NL-dev',true,'bu-1','dep-3','team-2','AGENT');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('AGENT5', 'NL-dev',true,'bu-2','dep-3','team-2','AGENT');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('AGENT6', 'NL-PS-dev',true,'bu-1','dep-3','team-2','AGENT');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('ADMIN1', 'NL-dev',true,'bu-1','dep-1','team-1','ADMIN');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('ACCOUNT_ADMIN1', 'NL-dev',true,'bu-1','dep-1','team-1','ACCOUNT_ADMIN');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('OPS_ADMIN', 'NL-dev',true,'bu-1','dep-1','team-1','OPS_ADMIN');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('SUPERVISOR1', 'NL-dev',true,'bu-1','dep-1','team-1','SUPERVISOR');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('MONITORING1', 'NL-dev',true,'bu-1','dep-2','team-1','MONITORING');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('MONITORING2', 'NL-dev',true,'bu-1','dep-1','team-2','MONITORING');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('MONITORING3', 'NL-PS-dev',true,'bu-2','dep-1','team-2','MONITORING');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('EU33FG', 'NL-dev',true,'bu-2','dep-1','team-2','OPS_ADMIN,ACCOUNT_ADMIN,AGENT,ADMIN,CJE');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions,allowed_channels,worker_sid) values ('123', 'NL-itest',true,'bu-2','dep-1','team-2','MONITORING,AGENT,ADMIN,SUPERVISOR,CJE,ACCOUNT_ADMIN,CONFIG_MANAGER',{(1,'team-2',2,'dep-1',3,'bu-2',true)},{'chat':1,'voice':1,'custom2':1},'WKe0cff64a467fec995d9eadac8da862df');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions,allowed_channels,worker_sid) values ('123', 'NL-dev',true,'bu-2','dep-1','team-2','MONITORING,AGENT,ADMIN,SUPERVISOR,CJE,ACCOUNT_ADMIN,CONFIG_MANAGER',{(1,'team-2',2,'dep-1',3,'bu-2',true)},{'chat':1,'voice':1,'custom2':1},'WKe0cff64a467fec995d9eadac8da862df');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions,allowed_channels,worker_sid) values ('BR68YP', 'NL-itest',true,'bu-2','dep-1','team-2','MONITORING,AGENT,ADMIN,SUPERVISOR,CJE,ACCOUNT_ADMIN',{(1,'team-2',2,'dep-1',3,'bu-2',true)},{'chat':1,'voice':1,'custom2':1,'custom3':1},'WK87b2fe56638909157da764f7cab17a68');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions,allowed_channels,worker_sid) values ('500', 'NL-itest',true,'bu-2','dep-1','team-2','MONITORING,AGENT,ADMIN,SUPERVISOR,CJE,ACCOUNT_ADMIN',{(1,'team-2',2,'dep-1',3,'bu-2',true)},{'chat':1,'voice':1,'custom2':1},'WK89b2fe56638909157da764f7cab17a69');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions,allowed_channels,worker_sid) values ('SUPERVISOR', 'NL-itest',true,'bu-2','dep-1','team-2','SUPERVISOR',{(28,'CLT5',23,'C2',21,'SC1',true)},{'chat':1,'voice':1,'custom2':1},'WK89b2fe56638909157da764f7cab17a69');
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FR-123', 'FR-itest',true,'bu-2','dep-1','team-2','MONITORING,AGENT,ADMIN,SUPERVISOR,CJE,ACCOUNT_ADMIN',{(28,'CLT5',23,'C2',21,'SC1',true)});

insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('SUPERVISOR_USER', 'NL-itest',true, 'SC1','C2','CLT5','SUPERVISOR,ACCOUNT_ADMIN',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('OPS_ADMIN', 'NL-itest',true, 'SC1','C2','CLT5','OPS_ADMIN',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FT_AGENT1', 'NL-itest', true, 'SC1','C2','CLT5','AGENT',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FT_AGENT2', 'NL-itest', true, 'SC1','C2','CLT5','AGENT',{(39,'CLT_5',33,'C_2',31,'SC_1',true)});
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FT_AGENT3', 'NL-itest', true, 'SC1','C2','CLT5','AGENT',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FT_CJE', 'NL-itest', true, 'SC1','C2','CLT5','CJE',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FT_ADMIN', 'NL-itest', true, 'SC1','C2','CLT5','ADMIN,CONFIG_MANAGER',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });
insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles,organisational_restrictions) values ('FT_MONITORING', 'NL-itest', true, 'SC1','C2','CLT5','MONITORING',{(28,'CLT5',23,'C2',21,'SC1',true),(36,'CLT_3', 32,'C_1',31,'SC_1',false) });

insert into contacting.employees_by_account (employee_id, account_friendly_name,preferred_account,business_unit,department,team,roles) values ('CJE', 'NL-dev',true,'bu-1','dep-1','team-1','CJE');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'login' , 'CJE' , -1 ,'ACCOUNT');
insert into contacting.business_functions_on_teams(account_friendly_name,business_function,role, organisation_id ,restriction) values('NL-dev', 'attribute management' , 'CJE' , -1 ,'ACCOUNT');

INSERT INTO contacting.contacting_configs(key, values) VALUES ('BUSINESS_FUNCTIONS', 'customer tooling for open,customer tooling for closed,contact handling,chat,chat-open,real time dashboard,user administration,organization administration,permissions administration,task routing management,callflow management,callflow audio management,announcement management,recording search & playback,recording playback selective,recording playback compliance,recording download,recording management,campaign management,login,agent statistics download,forecast upload,recording playback technical,opening hours management,attribute management,blacklist management,survey management,config management');
INSERT INTO contacting.contacting_configs(key, values) VALUES ('BUSINESS_FUNCTIONS_HIDDEN', 'system tooling,permissions administration');
INSERT INTO contacting.contacting_configs(key, values) VALUES ('ROLES', 'CUSTOMER_AUTHENTICATED,CUSTOMER_UNAUTHENTICATED,CONTACTING,AGENT,SUPERVISOR,ADMIN,MONITORING,ACCOUNT_ADMIN,COMPLIANCE_OFFICER,CAMPAIGN_MANAGER,COACH,CJE,ANNOUNCEMENT_MANAGER,OPS_ADMIN,STAFF_MANAGER,CONFIG_MANAGER');
INSERT INTO contacting.contacting_configs(key, values) VALUES ('RESTRICTIONS', 'NONE,SELF,TEAM,CIRCLE,SUPER_CIRCLE,ACCOUNT');
INSERT INTO contacting.contacting_configs(key, values) VALUES ('BUSINESS_FUNCTIONS_AT_TEAM_LEVEL', 'real time dashboard,user administration');


INSERT INTO contacting.contacting_configs(key, values) VALUES ('MAINTENANCE_MODE_ORACLE', 'false');
