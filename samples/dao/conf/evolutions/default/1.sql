# --- !Ups

create table "CAT" ("NAME" VARCHAR NOT NULL PRIMARY KEY,"COLOR" VARCHAR NOT NULL);

# --- !Downs

drop table "CAT";

