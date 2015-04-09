# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "CAT" ("NAME" VARCHAR NOT NULL PRIMARY KEY,"COLOR" VARCHAR NOT NULL);
create table "DOG" ("NAME" VARCHAR NOT NULL PRIMARY KEY,"COLOR" VARCHAR NOT NULL);

# --- !Downs

drop table "CAT";
drop table "DOG";
