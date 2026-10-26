package br.espm.msav02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequest(
        @Schema(example = "user") @NotBlank String username,
        @Schema(example = "user123") @NotBlank String senha) {
}
