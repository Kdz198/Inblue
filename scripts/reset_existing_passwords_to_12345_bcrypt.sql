-- Run this once after deploying the BCrypt password code.
-- It resets every existing User and Mentor password to: 12345
-- BCrypt hash generated with Spring Security BCryptPasswordEncoder strength 10.

START TRANSACTION;

UPDATE users
SET password = '$2a$10$REq9.6mnBOY3MuIUT.St9e.tFFWvGKXNBx961Mb3v4E7tUVYD/8ou'
WHERE password IS NOT NULL;

UPDATE Mentor
SET password = '$2a$10$REq9.6mnBOY3MuIUT.St9e.tFFWvGKXNBx961Mb3v4E7tUVYD/8ou'
WHERE password IS NOT NULL;

COMMIT;
