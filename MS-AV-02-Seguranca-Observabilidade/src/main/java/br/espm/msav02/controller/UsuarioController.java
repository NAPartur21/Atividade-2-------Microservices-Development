package br.espm.msav02.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.espm.msav02.dto.LoginRequest;
import br.espm.msav02.dto.MensagemResponse;
import br.espm.msav02.dto.NovoUsuarioRequest;
import br.espm.msav02.dto.TrocarSenhaRequest;
import br.espm.msav02.dto.UsuarioResponse;
import br.espm.msav02.service.LoginService;
import br.espm.msav02.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Usuários", description = "Login, cadastro, troca de senha e desbloqueio de usuários")
public class UsuarioController {

    private final LoginService loginService;
    private final UsuarioService usuarioService;

    public UsuarioController(LoginService loginService, UsuarioService usuarioService) {
        this.loginService = loginService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetua login (público)",
            description = "Valida usuário e senha na tabela usuarios. A partir da 2ª falha seguida o usuário é bloqueado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário e senha válidos"),
            @ApiResponse(responseCode = "401", description = "Usuário/senha inválidos ou usuário bloqueado",
                    content = @Content)
    })
    public ResponseEntity<MensagemResponse> login(@Valid @RequestBody LoginRequest request) {
        return switch (loginService.login(request.username(), request.senha())) {
            case SUCESSO -> ResponseEntity.ok(new MensagemResponse("Login realizado com sucesso"));
            case CREDENCIAIS_INVALIDAS -> naoAutorizado("Usuário ou senha inválidos");
            case INVALIDO_E_BLOQUEADO -> naoAutorizado("Usuário ou senha inválidos. Usuário bloqueado");
            case BLOQUEADO -> naoAutorizado("Usuário bloqueado. Procure um administrador");
        };
    }

    @PutMapping("/trocarsenha/{userid}")
    @Operation(summary = "Troca a senha de um usuário (público)",
            description = "Confere a senha atual no banco antes de gravar a nova.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Nova senha inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Senha atual incorreta", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    public ResponseEntity<MensagemResponse> trocarSenha(@PathVariable String userid,
            @Valid @RequestBody TrocarSenhaRequest request) {
        usuarioService.trocarSenha(userid, request.atual(), request.nova());
        return ResponseEntity.ok(new MensagemResponse("Senha alterada com sucesso"));
    }

    @PostMapping("/")
    @Operation(summary = "Cadastra um novo usuário (ROLE_MANUTENCAO)",
            description = "O novo usuário é criado com ROLE_USER.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuário já existe", content = @Content)
    })
    public ResponseEntity<MensagemResponse> cadastrar(@Valid @RequestBody NovoUsuarioRequest request) {
        usuarioService.cadastrar(request.username(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponse("Usuário cadastrado com sucesso"));
    }

    @PutMapping("/unlock/{userid}")
    @Operation(summary = "Desbloqueia um usuário (ROLE_ADMIN)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    public ResponseEntity<MensagemResponse> desbloquear(@PathVariable String userid) {
        usuarioService.desbloquear(userid);
        return ResponseEntity.ok(new MensagemResponse("Usuário desbloqueado"));
    }

    @GetMapping("/")
    @Operation(summary = "Lista todos os usuários (ROLE_MANUTENCAO)",
            description = "Não devolve a senha.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content)
    })
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    private ResponseEntity<MensagemResponse> naoAutorizado(String mensagem) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensagemResponse(mensagem));
    }
}
