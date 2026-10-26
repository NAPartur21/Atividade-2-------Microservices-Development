package br.espm.msav02.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.espm.msav02.dto.UsuarioResponse;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> buscarSenha(String username) {
        List<String> senhas = jdbc.queryForList("select senha from usuarios where username = ?", String.class, username);
        return senhas.stream().findFirst();
    }

    public void inserir(String username, String senha, String role) {
        jdbc.update("insert into usuarios (username, senha) values (?, ?)", username, senha);
        jdbc.update("insert into authorities (username, authority) values (?, ?)", username, role);
    }

    public void atualizarSenha(String username, String nova) {
        jdbc.update("update usuarios set senha = ? where username = ?", nova, username);
    }

    public List<UsuarioResponse> listar() {
        Map<String, List<String>> rolesPorUsuario = new HashMap<>();
        jdbc.query("select username, authority from authorities order by authority", rs -> {
            rolesPorUsuario.computeIfAbsent(rs.getString("username"), k -> new ArrayList<>())
                    .add(rs.getString("authority"));
        });

        return jdbc.query("""
                select u.username,
                       coalesce(l.locked, false) as locked,
                       coalesce(l.failed_attempts, 0) as failed_attempts
                from usuarios u
                left join login_attempts l on l.username = u.username
                order by u.username
                """,
                (rs, i) -> new UsuarioResponse(
                        rs.getString("username"),
                        rs.getBoolean("locked"),
                        rs.getInt("failed_attempts"),
                        rolesPorUsuario.getOrDefault(rs.getString("username"), List.of())));
    }
}
