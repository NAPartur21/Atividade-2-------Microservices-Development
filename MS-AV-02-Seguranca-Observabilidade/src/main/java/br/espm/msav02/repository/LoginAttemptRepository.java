package br.espm.msav02.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Acesso à tabela login_attempts (tentativas falhas e bloqueio). */
@Repository
public class LoginAttemptRepository {

    private final JdbcTemplate jdbc;

    public LoginAttemptRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean estaBloqueado(String username) {
        List<Boolean> locked = jdbc.queryForList(
                "select locked from login_attempts where username = ?", Boolean.class, username);
        return !locked.isEmpty() && Boolean.TRUE.equals(locked.get(0));
    }

    /** Soma uma falha (criando a linha se necessário) e devolve o total de falhas seguidas. */
    public int registrarFalha(String username) {
        Timestamp agora = Timestamp.from(Instant.now());
        int atualizadas = jdbc.update(
                "update login_attempts set failed_attempts = failed_attempts + 1, last_failed_at = ?, updated_at = ? where username = ?",
                agora, agora, username);
        if (atualizadas == 0) {
            jdbc.update(
                    "insert into login_attempts (username, failed_attempts, last_failed_at, updated_at) values (?, 1, ?, ?)",
                    username, agora, agora);
        }
        Integer total = jdbc.queryForObject(
                "select failed_attempts from login_attempts where username = ?", Integer.class, username);
        return total == null ? 0 : total;
    }

    public void bloquear(String username) {
        Timestamp agora = Timestamp.from(Instant.now());
        jdbc.update("update login_attempts set locked = true, locked_at = ?, updated_at = ? where username = ?",
                agora, agora, username);
    }

    /** Login válido: zera o contador de falhas seguidas. */
    public void zerarFalhas(String username) {
        jdbc.update("update login_attempts set failed_attempts = 0, updated_at = ? where username = ?",
                Timestamp.from(Instant.now()), username);
    }

    public void desbloquear(String username) {
        jdbc.update(
                "update login_attempts set locked = false, failed_attempts = 0, locked_at = null, updated_at = ? where username = ?",
                Timestamp.from(Instant.now()), username);
    }

    public Integer contarBloqueados() {
        Integer total = jdbc.queryForObject("select count(*) from login_attempts where locked = true", Integer.class);
        return total == null ? 0 : total;
    }
}
