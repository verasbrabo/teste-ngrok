-- Execute uma vez no banco existente antes de cadastrar o mesmo contato nos
-- dois aplicativos. O script localiza os nomes gerados pelo Hibernate para as
-- uniques antigas, remove somente os indices de uma coluna e cria as novas
-- restricoes compostas.

DROP PROCEDURE IF EXISTS remover_unique_simples_usuario;

DELIMITER $$
CREATE PROCEDURE remover_unique_simples_usuario(IN nome_coluna VARCHAR(64))
BEGIN
    DECLARE finalizado INT DEFAULT 0;
    DECLARE nome_indice VARCHAR(64);
    DECLARE indices CURSOR FOR
        SELECT indice
        FROM (
            SELECT INDEX_NAME AS indice
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'usuario'
              AND NON_UNIQUE = 0
              AND INDEX_NAME <> 'PRIMARY'
            GROUP BY INDEX_NAME
            HAVING GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = nome_coluna
        ) encontrados;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finalizado = 1;

    OPEN indices;
    leitura: LOOP
        FETCH indices INTO nome_indice;
        IF finalizado = 1 THEN
            LEAVE leitura;
        END IF;
        SET @sql_drop = CONCAT(
            'ALTER TABLE `usuario` DROP INDEX `',
            REPLACE(nome_indice, '`', '``'),
            '`'
        );
        PREPARE comando_drop FROM @sql_drop;
        EXECUTE comando_drop;
        DEALLOCATE PREPARE comando_drop;
    END LOOP;
    CLOSE indices;
END$$
DELIMITER ;

CALL remover_unique_simples_usuario('login');
CALL remover_unique_simples_usuario('telefone');
DROP PROCEDURE remover_unique_simples_usuario;

SET @possui_login_role = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuario'
      AND INDEX_NAME = 'uk_usuario_login_role'
);
SET @sql_login_role = IF(
    @possui_login_role = 0,
    'ALTER TABLE `usuario` ADD CONSTRAINT `uk_usuario_login_role` UNIQUE (`login`, `role`)',
    'SELECT 1'
);
PREPARE comando_login_role FROM @sql_login_role;
EXECUTE comando_login_role;
DEALLOCATE PREPARE comando_login_role;

SET @possui_telefone_role = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuario'
      AND INDEX_NAME = 'uk_usuario_telefone_role'
);
SET @sql_telefone_role = IF(
    @possui_telefone_role = 0,
    'ALTER TABLE `usuario` ADD CONSTRAINT `uk_usuario_telefone_role` UNIQUE (`telefone`, `role`)',
    'SELECT 1'
);
PREPARE comando_telefone_role FROM @sql_telefone_role;
EXECUTE comando_telefone_role;
DEALLOCATE PREPARE comando_telefone_role;
