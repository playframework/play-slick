# Account schema

# --- !Ups

CREATE TABLE records (
    id    INTEGER NOT NULL PRIMARY KEY,
    name  VARCHAR(256) NOT NULL UNIQUE
);

# --- !Downs

DROP TABLE records;
