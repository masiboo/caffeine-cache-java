--liquibase formatted sql

--changeset dinchand:prefill_settings_meta  dbms:oracle
INSERT INTO "GCONTACTINGAPI_OWNER"."SETTINGS_METADATA" (NAME, INPUT_TYPE, REGEX, CAPABILITY, CONSUMERS)
    (Select
         KEY AS NAME,
         CASE COUNT (UNIQUE (VALUE)) WHEN 1 THEN 'DROPDOWN'
             WHEN 2 THEN 'RADIO'
             WHEN 3 THEN 'DROPDOWN'
             WHEN 4 THEN 'DROPDOWN'
         ELSE 'TEXTBOX' END AS TYPES,
         CASE COUNT (UNIQUE (VALUE))
             WHEN 1 THEN NULL
             WHEN 2 THEN NULL
             WHEN 3 THEN NULL
             WHEN 4 THEN NULL
         ELSE 'TEXTBOX' END AS REGEX,
         MIN ( CAPABILITY) AS CAPABILITY,
         MIN ( CONSUMERS) AS CONSUMERS
     FROM
         (SELECT KEY, VALUE, CAPABILITY, CONSUMERS FROM account_settings
         UNION ALL
         SELECT KEY, VALUE, CAPABILITY, null AS CONSUMERS FROM organisation_settings)
     GROUP BY KEY);

--changeset dinchand:prefill_settings_meta_options  dbms:oracle
INSERT INTO settings_metadata_options (settings_meta_id, value, display_name)
    (Select
         MAX(ID), value, value
     FROM
         (SELECT KEY, VALUE FROM account_settings
          UNION ALL
          SELECT KEY,VALUE FROM organisation_settings) js JOIN settings_metadata sm  ON sm.name = js.key WHERE sm.input_type != 'TEXTBOX'
     GROUP BY KEY, VALUE);

--changeset dinchand:update_wfh_url dbms:oracle
UPDATE connections_details
SET edge_location = 'ie1-tnx' ,
    url ='wss://chunderw-vpc-gll-ie1-tnx.twilio.com'
WHERE connection_type='workFromHome' AND url ='wss://chunderw-vpc-gll-ie1.twilio.com'

--changeset dinchand:fix_settings dbms:oracle
UPDATE SETTINGS_METADATA SET regex = '^[0-9+]*$' WHERE NAME='DEFAULT_OUTBOUND_NUMBER';
UPDATE SETTINGS_METADATA SET regex = '^[a-zA-Z\/]*$' WHERE NAME='TIMEZONE';
UPDATE SETTINGS_METADATA SET regex = '^[a-zA-Z0-9 -]*$' WHERE NAME='TELEOPTI_BUSINESS_UNIT_NAME';
DROP INDEX ACCOUNT_SETTINGS_KEY_VALUE;
CREATE UNIQUE INDEX ACCOUNT_SETTINGS_KEY_VALUE ON ACCOUNT_SETTINGS (KEY,ACCOUNT_ID);
insert into ACCOUNT_SETTINGS (id, key, value, account_id)  Select ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OUTBOUND_CALL_FROM_AVAILABLE', ' ',id from(Select id from accounts a where a.id NOT IN(Select account_id FROM account_settings where key='OUTBOUND_CALL_FROM_AVAILABLE'));

--changeset gjcompagner:INC7447159
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'scrollToFirstCustomerMessage', 'RADIO', 'chat', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'scrollToFirstCustomerMessage'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'scrollToFirstCustomerMessage'),'FALSE' ,'FALSE');


--changeset pgogoi:STRY3496488
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'PREVIEW_DIALER_EDITABLE_NUMBER', 'RADIO', 'dialer', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'PREVIEW_DIALER_EDITABLE_NUMBER'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'PREVIEW_DIALER_EDITABLE_NUMBER'),'FALSE' ,'FALSE');

--changeset pgogoi:STRY3522409_feature_settings:oracle failOnError:true
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'DEBUG_TASKROUTER_SDK', 'RADIO', 'omnichannel toolbar', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'DEBUG_TASKROUTER_SDK'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'DEBUG_TASKROUTER_SDK'),'FALSE' ,'FALSE');

insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'SYNC_SDK_LOGLEVEL', 'RADIO', 'omnichannel toolbar', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'SYNC_SDK_LOGLEVEL'),'silent' ,'Silent');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'SYNC_SDK_LOGLEVEL'),'error' ,'Error');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'SYNC_SDK_LOGLEVEL'),'warn' ,'Warn');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'SYNC_SDK_LOGLEVEL'),'info' ,'Info');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'SYNC_SDK_LOGLEVEL'),'debug' ,'Debug');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'SYNC_SDK_LOGLEVEL'),'trace' ,'Trace');

insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'VOICE_SDK_LOGLEVEL', 'RADIO', 'omnichannel toolbar', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VOICE_SDK_LOGLEVEL'),'silent' ,'Silent');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VOICE_SDK_LOGLEVEL'),'error' ,'Error');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VOICE_SDK_LOGLEVEL'),'warn' ,'Warn');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VOICE_SDK_LOGLEVEL'),'info' ,'Info');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VOICE_SDK_LOGLEVEL'),'debug' ,'Debug');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VOICE_SDK_LOGLEVEL'),'trace' ,'Trace');

--changeset pgogoi:maxInactivityDuration:oracle failOnError:true
DELETE FROM SETTINGS_METADATA_OPTIONS WHERE SETTINGS_META_ID=(select id from SETTINGS_METADATA where name = 'asyncChat.maxInactivityDuration');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'asyncChat.maxInactivityDuration'),'172800000' ,'48 Hours');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'asyncChat.maxInactivityDuration'),'259200000' ,'72 Hours');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'asyncChat.maxInactivityDuration'),'345600000' ,'96 Hours');

--changeset dinchand:local_breakout_nl_suriname:oracle failOnError:true
UPDATE connections_details SET url = 'https://event-bridge.twilio.com' where id in (Select cd.id from connections_details cd
                                                                                                          LEFT OUTER JOIN connections c ON c.id = cd.connection_id
                                                                                                          LEFT OUTER JOIN accounts a ON a.id = c.account_id
                                                                                    where a.friendlyname = concat('NL-', (select env from environment)) and c.domain = 'taskrouter' and c.layer = 'frontend') ;

UPDATE connections_details SET url = 'wss://tsock.twilio.com/v3/wsconnect' where id in (Select cd.id from connections_details cd
                                                                                                              LEFT OUTER JOIN connections c ON c.id = cd.connection_id
                                                                                                              LEFT OUTER JOIN accounts a ON a.id = c.account_id
                                                                                        where a.friendlyname = concat('NL-', (select env from environment)) and c.domain = 'sync' and c.layer = 'frontend') ;


--changeset pgogoi:INC8067397_settings_meta
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'VideoMeetingIDCreationAllowed', 'RADIO', 'video', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VideoMeetingIDCreationAllowed'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'VideoMeetingIDCreationAllowed'),'FALSE' ,'FALSE');

--changeset dinchand:tata_callflow_DE
INSERT INTO PLATFORM_ACCOUNT_SETTINGS(ID, KEY, VALUE, ACCOUNT_ID) VALUES(PLATFORM_ACC_SET_SEQUENCE.NEXTVAL, 'callFlowTataClientId', 'eae9b82e-7ef6-45cb-b33b-420e07bd3497', (select id from accounts where friendlyName = concat('DE-', (select env from environment))));

--changeset dinchand:KAN0671894
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'Chat_TemporarilyClosedByBusiness', 'RADIO', 'chat', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'Chat_TemporarilyClosedByBusiness'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'Chat_TemporarilyClosedByBusiness'),'FALSE' ,'FALSE');

--changeset dinchand:KAN0679121
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'Chat_Templates_FindPreviewSelectEnabled', 'RADIO', 'chat', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'Chat_Templates_FindPreviewSelectEnabled'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'Chat_Templates_FindPreviewSelectEnabled'),'FALSE' ,'FALSE');

--changeset ayushm:INC9258014
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,REGEX,CAPABILITY,CONSUMERS) values (null,'PUSH_USERS_FROM_RONA_TO_AVAILABLE','TEXTBOX','[A-z]','generic','employee');

--changeset ayushm:INC9258014-fix
update SETTINGS_METADATA set regex = '^[0-9]\d{0,2}$' where NAME = 'PUSH_USERS_FROM_RONA_TO_AVAILABLE';

--changeset ayushm:client_id_tata
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,REGEX,CAPABILITY,CONSUMERS) values (null,'CLIENT_ID_FOR_TATA_OTP','TEXTBOX','^[0-9a-fA-F]{8}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{4}\-[0-9a-fA-F]{12}$','generic','employee');

--changeset ayushm:rosPreviewMode
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_PREVIEW_MODE', 'RADIO', 'chat', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_PREVIEW_MODE'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_PREVIEW_MODE'),'FALSE' ,'FALSE');
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_PREVIEW_MODE', 'FALSE', 'chat','employee',(select id from accounts where friendlyName = concat('NL-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_PREVIEW_MODE', 'FALSE', 'chat','employee',(select id from accounts where friendlyName = concat('BE-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_PREVIEW_MODE', 'FALSE', 'chat','employee',(select id from accounts where friendlyName = concat('ES-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_PREVIEW_MODE', 'FALSE', 'chat','employee',(select id from accounts where friendlyName = concat('DE-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_PREVIEW_MODE', 'FALSE', 'chat','employee',(select id from accounts where friendlyName = concat('RO-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_PREVIEW_MODE', 'FALSE', 'chat','employee',(select id from accounts where friendlyName = concat('GSHR-', (select env from environment))));

--changeset massimo:OCT_AUTO_STOP_ACW_TIME_INSERT
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,REGEX,CAPABILITY,CONSUMERS) values (null,'OCT_AUTO_STOP_ACW_TIME','TEXTBOX','^[0-9]*$','omnichannel toolbar','employee');

--changeset massimo:OCT_MAKE_OTHER_AGENT_AS_CONF_HOST_INSERT
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_MAKE_OTHER_AGENT_AS_CONF_HOST', 'RADIO', 'chat', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_MAKE_OTHER_AGENT_AS_CONF_HOST'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_MAKE_OTHER_AGENT_AS_CONF_HOST'),'FALSE' ,'FALSE');

--changeset ayushm:TASK3542809
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'RADIO', 'omnichannel toolbar', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_WS_USE_NGNIX_ROUTE_URLS'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_WS_USE_NGNIX_ROUTE_URLS'),'FALSE' ,'FALSE');
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('NL-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('BE-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('ES-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('DE-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('RO-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('GSHR-', (select env from environment))));
insert into ACCOUNT_SETTINGS (ID, key, value, CAPABILITY, CONSUMERS, ACCOUNT_ID)
values (ACCOUNT_SETTINGS_SEQUENCE.NEXTVAL, 'OCT_WS_USE_NGNIX_ROUTE_URLS', 'FALSE', 'omnichannel toolbar','employee',(select id from accounts where friendlyName = concat('IT-', (select env from environment))));

--changeset ayushm:TASK3537801
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'Chat_Translation_TranslationSupportEnabled', 'RADIO', 'omnichannel toolbar', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'Chat_Translation_TranslationSupportEnabled'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'Chat_Translation_TranslationSupportEnabled'),'FALSE' ,'FALSE');

--changeset ayushm:3725878_3720522
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_LOG_API_CALLS', 'RADIO', 'generic', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_LOG_API_CALLS'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_LOG_API_CALLS'),'FALSE' ,'FALSE');
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_PREVIEWDIALER_SINGLESTEP_DIAL_DIRECTLY', 'RADIO', 'generic', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_PREVIEWDIALER_SINGLESTEP_DIAL_DIRECTLY'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_PREVIEWDIALER_SINGLESTEP_DIAL_DIRECTLY'),'FALSE' ,'FALSE');
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_LOG_LEVEL', 'RADIO', 'generic', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_LOG_LEVEL'),'info' ,'Info');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_LOG_LEVEL'),'error' ,'Error');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_LOG_LEVEL'),'warn' ,'Warn');
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,REGEX,CAPABILITY,CONSUMERS) values (null,'Chat_Attachments_RetentionPeriodInDays','TEXTBOX','^(0|[1-9]|[1-9][0-9]|1[0-7][0-9]|180)$','chat','api');

--changeset massimo:add_2_new_setting_option_iris
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'custom3_association'),'{}' ,'{}');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'chat_association'),'{}' ,'{}');
UPDATE SETTINGS_METADATA SET INPUT_TYPE = 'RADIO' where name = 'custom3_association';
UPDATE SETTINGS_METADATA SET INPUT_TYPE = 'RADIO' where name = 'chat_association';

--changeset martine:TASK4941253
insert into SETTINGS_METADATA (ID, NAME,INPUT_TYPE,CAPABILITY,CONSUMERS) values (null, 'OCT_SMALLBOARD_HIDE', 'RADIO', 'omnichannel toolbar', 'employee');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_SMALLBOARD_HIDE'),'TRUE' ,'TRUE');
insert into SETTINGS_METADATA_OPTIONS (ID, SETTINGS_META_ID,VALUE,DISPLAY_NAME) values (null, (select id from SETTINGS_METADATA where name = 'OCT_SMALLBOARD_HIDE'),'FALSE' ,'FALSE');

--changeset martine:TASK5311131
update SETTINGS_METADATA set CAPABILITY = 'generic' where CAPABILITY = 'omnichannel toolbar';
