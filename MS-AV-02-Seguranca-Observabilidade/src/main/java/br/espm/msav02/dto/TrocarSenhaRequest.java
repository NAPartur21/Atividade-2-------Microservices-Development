package br.espm.msav02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Troca de senha")
public record TrocarSenhaRequest(
        @Schema(example = "user123") @NotBlank String atual,
        @Schema(example = "nova123", description = "Máximo de 10 caracteres")
        @NotBlank @Size(max = 10) String nova) {
}
