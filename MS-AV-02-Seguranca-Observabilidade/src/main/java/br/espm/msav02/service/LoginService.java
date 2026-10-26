package br.espm.msav02.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.espm.msav02.metrics.LoginMetrics;
import br.espm.msav02.repository.LoginAttemptRepository;
import br.espm.msav02.repository.UsuarioRepository;

@Service
public class LoginService {

    /** Falhou "mais de uma vez" => a partir da 2ª falha seguida o usuário é bloqueado. */
    private static final int MAX_FALHAS_PERMITIDAS = 1;

    public enum Resultado {
        SUCESSO,
        CREDENCIAIS_INVALIDAS,
        INVALIDO_E_BLOQUEADO,
        BLOQUEADO
    }

    private final UsuarioRepository usuarios;
    private final LoginAttemptRepository attempts;
    private final LoginMetrics metrics;

    public LoginService(UsuarioRepository usuarios, LoginAttemptRepository attempts, LoginMetrics metrics) {
        this.usuarios = usuarios;
        this.attempts = attempts;
        this.metrics = metrics;
    }

    @Transactional
    public Resultado login(String username, String senha) {
        Optional<String> senhaCadastrada = usuarios.buscarSenha(username);

        // usuário inexistente: inválido, mas não cria linha em login_attempts
        if (senhaCadastrada.isEmpty()) {
            metrics.registrarLoginInvalido();
            return Resultado.CREDENCIAIS_INVALIDAS;
        }

        if (attempts.estaBloqueado(username)) {
            metrics.registrarLoginInvalido();
            return Resultado.BLOQUEADO;
        }

        if (senhaCadastrada.get().equals(senha)) {
            attempts.zerarFalhas(username);
            metrics.registrarLoginValido();
            return Resultado.SUCESSO;
        }

        int falhas = attempts.registrarFalha(username);
        metrics.registrarLoginInvalido();
        if (falhas > MAX_FALHAS_PERMITIDAS) {
            attempts.bloquear(username);
            return Resultado.INVALIDO_E_BLOQUEADO;
        }
        return Resultado.CREDENCIAIS_INVALIDAS;
    }
}
