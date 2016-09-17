create database if not exists android_api;
 
use android_api;
 
drop table if exists users;
create table users(
   id int(11) primary key auto_increment,
   unique_id varchar(23) not null unique,
   name varchar(50) not null,
   email varchar(100) not null unique,
   encrypted_password varchar(80) not null,
   salt varchar(10) not null,
   created_at datetime,
   updated_at datetime null,
   session varchar(50)
);

drop table if exists events;
create table events(
   id int(11) primary key auto_increment,
   unique_id varchar(23) not null unique,
   name varchar(50) not null,
   owner int(11) not null,
   whenEvent bigint(30) not null,
   lastmodified datetime not null,
   longitude real not null,
   latitude real not null
);

drop table if exists participation;
create table participation(
    user_id int(11) NOT NULL,
    event_id int(11) NOT NULL,
    PRIMARY KEY (user_id, event_id),
    CONSTRAINT Constr_Participation_User_fk
	FOREIGN KEY (user_id) REFERENCES user(id)
	ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT Constr_Participation_Event_fk
	FOREIGN KEY (event_id) REFERENCES event (id)
	ON DELETE CASCADE ON UPDATE CASCADE
);


drop table if exists pending_users;
create table pending_users(
   id int(11) primary key auto_increment,
   email varchar(100) not null unique
);

drop table if exists pending_invites;
create table pending_invites(
    user_id int(11) NOT NULL,
    event_id int(11) NOT NULL,
    PRIMARY KEY (user_id, event_id),
    CONSTRAINT Constr_Participation_PUser_fk
	FOREIGN KEY (user_id) REFERENCES pending_user(id)
	ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT Constr_Participation_PEvent_fk
	FOREIGN KEY (event_id) REFERENCES event (id)
	ON DELETE CASCADE ON UPDATE CASCADE
);

drop table if exists comments;
create table comments(
	id int(11) primary key auto_increment,
	user_id int(11) NOT NULL,
	event_id int(11) NOT NULL,
	cdate real NOT NULL,
	post varchar(1000) NOT NULL,
	CONSTRAINT Constr_Comments_User_fk
		FOREIGN KEY (user_id) REFERENCES user(id)
		ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT Constr_Comments_Event_fk
		FOREIGN KEY (user_id) REFERENCES events(id)
		ON DELETE CASCADE ON UPDATE CASCADE
);
	
