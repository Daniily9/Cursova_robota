
-- Company Management System
-- PostgreSQL database schema
-- Variant 4

CREATE TABLE IF NOT EXISTS departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    salary NUMERIC(12,2) NOT NULL
        CHECK (salary >= 0),
    department_id INTEGER NOT NULL,
    CONSTRAINT fk_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE RESTRICT
);

-- Index for department-based employee queries
CREATE INDEX IF NOT EXISTS idx_employees_department_id
    ON employees(department_id);
