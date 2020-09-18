CREATE SCHEMA IB DEFAULT CHARACTER SET utf8;

CREATE TABLE USER(
	id INT AUTO_INCREMENT,
    email VARCHAR(50) NOT NULL,
	password VARCHAR(20) NOT NULL, 
	certificate VARCHAR(40) NOT NULL, 
	active INT(1),
	authority VARCHAR(20) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE AUTHORITY(
	id INT AUTO_INCREMENT,
	name VARCHAR(10),
    PRIMARY KEY(id)
);
INSERT INTO AUTHORITY (id, name) VALUES (1, 'Admin');
INSERT INTO AUTHORITY (id, name) VALUES (2, 'Korisnik');

INSERT INTO USER (id, email, password, certificate, active, authority) VALUES (1, 'usera@gmail.com','usera', '' , 1, 'Admin');
INSERT INTO USER (id, email, password, certificate, active, authority) VALUES (2, 'userb@gmail.com','userb', '' , 1, 'Korisnik');
