package br.espm.msav02.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.espm.msav02.dto.UsuarioResponse;
import br.espm.msav02.repository.LoginAttemptRepository;
import br.espm.msav02.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final String ROLE_PADRAO = "ROLE_USER";
    private static final int TAMANHO_MAX_SENHA = 10;

    private final UsuarioRepository usuarios;
    private final LoginAttemptRepository attempts;
    private final JdbcUserDetailsManager userManager;

    public UsuarioService(UsuarioRepository usuarios, LoginAttemptRepository attempts,
            JdbcUserDetailsManager userManager) {
        this.usuarios = usuarios;
        this.attempts = attempts;
        this.userManager = userManager;
    }

    @Transactional
    public void cadastrar(String username, String senha) {
        if (userManager.userExists(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já cadastrado");
        }
        validarNovaSenha(senha);
        usuarios.inserir(username, senha, ROLE_PADRAO);
    }

    @Transactional
    public void trocarSenha(String userid, String atual, String nova) {
        String senhaCadastrada = usuarios.buscarSenha(userid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!senhaCadastrada.equals(atual)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta");
        }
        validarNovaSenha(nova);
        usuarios.atualizarSenha(userid, nova);
    }

    @Transactional
    public void desbloquear(String userid) {
        if (!userManager.userExists(userid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        attempts.desbloquear(userid);
    }

    public List<UsuarioResponse> listar() {
        return usuarios.listar();
    }

    private void validarNovaSenha(String senha) {
        if (senha == null || senha.isBlank() || senha.length() > TAMANHO_MAX_SENHA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha deve ter de 1 a " + TAMANHO_MAX_SENHA + " caracteres");
        }
    }
}
