CREATE DATABASE IF NOT EXISTS employee_appraisal
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE employee_appraisal;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_name VARCHAR(120) NOT NULL,

    employee_code VARCHAR(50) NOT NULL UNIQUE,

    department VARCHAR(120),

    designation VARCHAR(120),

    current_role VARCHAR(120),

    date_of_joining DATE,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS form_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT NOT NULL,

    response_type VARCHAR(40) NOT NULL,

    phase VARCHAR(50),

    question_key VARCHAR(100),

    answer LONGTEXT,

    challenge_name VARCHAR(150),

    original_file_name VARCHAR(255),

    stored_file_name VARCHAR(255),

    file_path VARCHAR(500),

    content_type VARCHAR(150),

    file_size BIGINT,

    points_awarded INT NOT NULL DEFAULT 0,

    submitted_at DATETIME NOT NULL,

    CONSTRAINT fk_response_employee
    FOREIGN KEY (employee_id)
    REFERENCES employees(id)
    ON DELETE CASCADE
);