drop table agency if exists;
create table agency (id bigint generated by default as identity, name varchar(20), balance decimal(10,2), primary key (id));
