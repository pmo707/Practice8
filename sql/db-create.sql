
DROP DATABASE IF EXISTS mydb;
CREATE DATABASE mydb;

USE mydb;

CREATE TABLE teams (
  id INT PRIMARY KEY auto_increment,
  name VARCHAR(15) UNIQUE NOT NULL
);

CREATE TABLE users (
  id INT PRIMARY KEY auto_increment,
  login VARCHAR(15) UNIQUE NOT NULL

);

CREATE TABLE users_teams(
  user_id  int,
  team_id int,
  foreign key (user_id) references users (id),
  foreign key (team_id) references teams (id) ON DELETE CASCADE,
primary key (user_id, team_id)
);

-- ---------------------

INSERT INTO teams VALUES (1, 'teamA');


INSERT INTO users VALUES (default, 'ivanov');

INSERT INTO users_teams VALUES (1, 1);


-- ----------------------

SELECT * FROM teams;
SELECT * FROM users;
SELECT * FROM users_teams;
