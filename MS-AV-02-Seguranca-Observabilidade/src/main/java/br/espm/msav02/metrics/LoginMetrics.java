package br.espm.msav02.metrics;

import org.springframework.stereotype.Component;

import br.espm.msav02.repository.LoginAttemptRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * 02 - Métricas
 *
 * Indicadores expostos em /actuator/prometheus:
 *   logins_validos_total    (counter) logins válidos
 *   logins_invalidos_total  (counter) logins inválidos
 *   usuarios_bloqueados     (gauge)   usuários bloqueados no momento (lido do banco a cada coleta)
 */
@Component
public class LoginMetrics {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetrics(MeterRegistry registry, LoginAttemptRepository attempts) {
        this.loginsValidos = Counter.builder("logins_validos")
                .description("Quantidade de logins válidos")
                .register(registry);

        this.loginsInvalidos = Counter.builder("logins_invalidos")
                .description("Quantidade de logins inválidos")
                .register(registry);

        Gauge.builder("usuarios_bloqueados", attempts::contarBloqueados)
                .description("Total de usuários bloqueados")
                .register(registry);
    }

    public void registrarLoginValido() {
        loginsValidos.increment();
    }

    public void registrarLoginInvalido() {
        loginsInvalidos.increment();
    }
}
