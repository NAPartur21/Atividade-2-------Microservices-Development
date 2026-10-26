package br.espm.msav02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um novo usuário")
public record NovoUsuarioRequest(
        @Schema(example = "aluno1") @NotBlank @Size(max = 100) String username,
        @Schema(example = "abc123", description = "Máximo de 10 caracteres (usuarios.senha VARCHAR(10))")
        @NotBlank @Size(max = 10) String senha) {
}
