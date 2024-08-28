CREATE TABLE IF NOT EXISTS scope (
    id integer NOT NULL PRIMARY KEY,
    scopeType varchar(42),
    scopeLevel integer
  );

CREATE TABLE IF NOT EXISTS cmsubscribedeventssubs (
    id integer NOT NULL PRIMARY KEY,
    notificationRecipientAddress varchar(300) NOT NULL,
    scopeId integer,
    notificationTypes varchar(300),
    notificationFilter varchar(5000),
    objectInstance varchar(600)NOT NULL,
    objectClass varchar(300)NOT NULL,
    CONSTRAINT "scope_fk" FOREIGN KEY (scopeId) REFERENCES scope (id)
  );


CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START 1;