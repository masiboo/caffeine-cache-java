-- Test schema for H2 database

CREATE TABLE CONNECTIONS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    DOMAIN VARCHAR(255) NOT NULL,
    LAYER VARCHAR(50) NOT NULL,
    ACCOUNT_ID BIGINT NOT NULL
);

CREATE TABLE CONNECTIONS_DETAILS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    CONNECTION_ID BIGINT NOT NULL,
    CONNECTION_TYPE VARCHAR(50) NOT NULL,
    EDGE_LOCATION VARCHAR(100) NOT NULL,
    URL VARCHAR(500) NOT NULL,
    REGION VARCHAR(100),
    CONSTRAINT fk_cd_connection FOREIGN KEY (CONNECTION_ID) REFERENCES CONNECTIONS(ID)
);

CREATE TABLE ACTIVE_CONNECTIONS (
    CONNECTION_ID BIGINT PRIMARY KEY,
    CONNECTION_DETAILS_ID BIGINT NOT NULL,
    CONSTRAINT fk_ac_connection FOREIGN KEY (CONNECTION_ID) REFERENCES CONNECTIONS(ID),
    CONSTRAINT fk_ac_connection_details FOREIGN KEY (CONNECTION_DETAILS_ID) REFERENCES CONNECTIONS_DETAILS(ID)
);

CREATE TABLE task_queues (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             assignmentactivityid VARCHAR(255),
                             enddate TIMESTAMP,
                             friendlyname VARCHAR(255),
                             last_updated_at TIMESTAMP,
                             last_updated_by VARCHAR(255),
                             reservationactivityid VARCHAR(255),
                             sid VARCHAR(255),
                             synced BOOLEAN,
                             targetworkers VARCHAR(255),
                             workspace_id BIGINT
);

CREATE TABLE survey_task_queue_mapping (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           survey_id BIGINT,
                                           task_queue_id BIGINT,
                                           FOREIGN KEY (task_queue_id) REFERENCES task_queues(id)
);

CREATE TABLE SETTINGS_METADATA (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL UNIQUE,
    INPUT_TYPE VARCHAR(50) NOT NULL,
    REGEX VARCHAR(500),
    CAPABILITY VARCHAR(255),
    CONSUMERS VARCHAR(255)
);

CREATE TABLE SETTINGS_METADATA_OPTIONS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SETTINGS_META_ID BIGINT NOT NULL,
    VALUE VARCHAR(255) NOT NULL,
    DISPLAY_NAME VARCHAR(255) NOT NULL,
    CONSTRAINT fk_settings_metadata FOREIGN KEY (SETTINGS_META_ID) REFERENCES SETTINGS_METADATA(ID),
    CONSTRAINT uq_settings_options UNIQUE (SETTINGS_META_ID, VALUE)
);
