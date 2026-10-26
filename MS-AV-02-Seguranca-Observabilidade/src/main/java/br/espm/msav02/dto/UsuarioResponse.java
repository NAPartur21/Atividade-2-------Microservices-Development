package br.espm.msav02.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuário cadastrado (a senha nunca é devolvida)")
public record UsuarioResponse(
        String username,
        boolean locked,
        int failedAttempts,
        List<String> roles) {
}
