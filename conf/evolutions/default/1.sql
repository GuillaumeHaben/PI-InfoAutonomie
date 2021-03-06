# --- !Ups

create table account (
  email                     varchar(255) not null,
  hashed_password           varchar(255),
  is_admin                  boolean,
  constraint pk_account primary key (email))
;

create table basic_event (
  id                        varchar(255) not null,
  name                      varchar(255),
  sensor_id                 varchar(255),
  detection_method_id       varchar(255),
  icon                      varchar(255),
  color                     varchar(255),
  constraint pk_basic_event primary key (id))
;

create table basic_event_occurrence (
  id                        varchar(255) not null,
  basic_event_id            varchar(255),
  timestamp                 bigint,
  date                      varchar(255),
  from_value                double,
  to_value                  double,
  constraint pk_basic_event_occurrence primary key (id))
;

create table data (
  timestamp                 bigint,
  mote                      varchar(255),
  label                     varchar(255),
  value                     double,
  constraint pk_data primary key (timestamp, mote, label))
;

create table detection (
  id                        varchar(255) not null,
  detection_type            integer,
  simple_threshold          double,
  min_value                 double,
  max_value                 double,
  delta                     double,
  constraint ck_detection_detection_type check (detection_type in (0,1,2)),
  constraint pk_detection primary key (id))
;

create table event (
  id                        varchar(255) not null,
  name                      varchar(255),
  duration                  integer,
  time_interval_id          varchar(255),
  expression                varchar(255),
  icon                      varchar(255),
  color                     varchar(255),
  constraint pk_event primary key (id))
;

create table event_occurrence (
  id                        varchar(255) not null,
  event_id                  varchar(255),
  timestamp                 bigint,
  date                      varchar(255),
  constraint pk_event_occurrence primary key (id))
;

create table sensor (
  id                        varchar(255) not null,
  name                      varchar(255),
  address                   varchar(255),
  type                      integer,
  location                  varchar(255),
  description               varchar(255),
  unit                      varchar(255),
  constraint ck_sensor_type check (type in (0,1,2,3,4,5)),
  constraint pk_sensor primary key (id))
;

create table time_interval (
  id                        varchar(255) not null,
  timestamp_start           bigint,
  timestamp_end             bigint,
  constraint pk_time_interval primary key (id))
;

create table token (
  id                        varchar(255) not null,
  token                     varchar(255),
  constraint pk_token primary key (id))
;


create table event_basic_event (
  event_id                       varchar(255) not null,
  basic_event_id                 varchar(255) not null,
  constraint pk_event_basic_event primary key (event_id, basic_event_id))
;
create sequence account_seq;

create sequence basic_event_seq;

create sequence basic_event_occurrence_seq;

create sequence data_seq;

create sequence detection_seq;

create sequence event_seq;

create sequence event_occurrence_seq;

create sequence sensor_seq;

create sequence time_interval_seq;

create sequence token_seq;

alter table basic_event add constraint fk_basic_event_sensor_1 foreign key (sensor_id) references sensor (id) on delete restrict on update restrict;
create index ix_basic_event_sensor_1 on basic_event (sensor_id);
alter table basic_event add constraint fk_basic_event_detectionMethod_2 foreign key (detection_method_id) references detection (id) on delete restrict on update restrict;
create index ix_basic_event_detectionMethod_2 on basic_event (detection_method_id);
alter table basic_event_occurrence add constraint fk_basic_event_occurrence_basi_3 foreign key (basic_event_id) references basic_event (id) on delete restrict on update restrict;
create index ix_basic_event_occurrence_basi_3 on basic_event_occurrence (basic_event_id);
alter table event add constraint fk_event_timeInterval_4 foreign key (time_interval_id) references time_interval (id) on delete restrict on update restrict;
create index ix_event_timeInterval_4 on event (time_interval_id);
alter table event_occurrence add constraint fk_event_occurrence_event_5 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_event_occurrence_event_5 on event_occurrence (event_id);



alter table event_basic_event add constraint fk_event_basic_event_event_01 foreign key (event_id) references event (id) on delete restrict on update restrict;

alter table event_basic_event add constraint fk_event_basic_event_basic_ev_02 foreign key (basic_event_id) references basic_event (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists account;

drop table if exists basic_event;

drop table if exists basic_event_occurrence;

drop table if exists data;

drop table if exists detection;

drop table if exists event;

drop table if exists event_basic_event;

drop table if exists event_occurrence;

drop table if exists sensor;

drop table if exists time_interval;

drop table if exists token;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists account_seq;

drop sequence if exists basic_event_seq;

drop sequence if exists basic_event_occurrence_seq;

drop sequence if exists data_seq;

drop sequence if exists detection_seq;

drop sequence if exists event_seq;

drop sequence if exists event_occurrence_seq;

drop sequence if exists sensor_seq;

drop sequence if exists time_interval_seq;

drop sequence if exists token_seq;

