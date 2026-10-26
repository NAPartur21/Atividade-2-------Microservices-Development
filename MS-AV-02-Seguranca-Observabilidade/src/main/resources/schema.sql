-- Tabelas do enunciado (MS-AV-02).
-- OBS: o script original tinha uma virgula sobrando depois de "updated_at ...," que gera erro de sintaxe;
-- ela foi removida aqui.

CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

-- Roles dos usuarios (lidas pelo JdbcUserDetailsManager)
CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR(100) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_usuarios FOREIGN KEY (username) REFERENCES usuarios (username),
    CONSTRAINT uk_authorities UNIQUE (username, authority)
);
