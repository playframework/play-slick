# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "CAT" ("name" VARCHAR NOT NULL PRIMARY KEY,"color" VARCHAR NOT NULL);

# --- !Downs

drop table "CAT";

