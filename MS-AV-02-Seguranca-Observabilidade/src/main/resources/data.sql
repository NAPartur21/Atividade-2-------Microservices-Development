-- Usuarios de teste (senha com no maximo 10 caracteres, por causa de usuarios.senha VARCHAR(10))
INSERT INTO usuarios (username, senha) VALUES ('admin', 'admin123');
INSERT INTO usuarios (username, senha) VALUES ('manut', 'manut123');
INSERT INTO usuarios (username, senha) VALUES ('user', 'user123');

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_MANUTENCAO');
INSERT INTO authorities (username, authority) VALUES ('manut', 'ROLE_MANUTENCAO');
INSERT INTO authorities (username, authority) VALUES ('user', 'ROLE_USER');
