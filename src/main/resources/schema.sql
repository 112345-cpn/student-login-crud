-- Administrator table. Administrators log in with name and password.
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE COMMENT 'administrator name/login name',
    password VARCHAR(255) NOT NULL COMMENT 'password or BCrypt hash'
);

-- Student table. Students do not have login credentials.
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    number CHAR(10) NOT NULL UNIQUE COMMENT '10 digits starting with 26',
    name VARCHAR(100) NOT NULL COMMENT 'student name',
    score DECIMAL(5,2) NULL COMMENT 'score',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'registration time'
);

-- For an existing student table, apply equivalent ALTER TABLE statements after checking current data.
