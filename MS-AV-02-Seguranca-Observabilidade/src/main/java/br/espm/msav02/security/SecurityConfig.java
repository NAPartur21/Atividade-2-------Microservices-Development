package br.espm.msav02.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 03 - Segurança
 *
 * - Usuários e roles vêm do banco via JdbcUserDetailsManager (tabelas usuarios + authorities).
 * - Usuário bloqueado (login_attempts.locked = true) aparece como "disabled" e não autentica.
 * - Endpoints protegidos usam HTTP Basic; /login e /trocarsenha são públicos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // públicos
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // com role
                        .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * JdbcUserDetailsManager apontando para as tabelas do enunciado:
     * a senha vem de usuarios.senha e "enabled" = usuário NÃO bloqueado.
     */
    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUsersByUsernameQuery("""
                select u.username, u.senha, not coalesce(l.locked, false)
                from usuarios u
                left join login_attempts l on l.username = u.username
                where u.username = ?
                """);
        manager.setAuthoritiesByUsernameQuery("select username, authority from authorities where username = ?");
        manager.setUserExistsSql("select username from usuarios where username = ?");
        return manager;
    }

    /**
     * usuarios.senha é VARCHAR(10) (definido no enunciado), então a senha fica em texto puro.
     * Em produção: BCryptPasswordEncoder + coluna maior (mínimo 60 caracteres).
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
