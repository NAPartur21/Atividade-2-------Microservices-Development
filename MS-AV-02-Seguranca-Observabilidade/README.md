# MS-AV-02 - Segurança e Observabilidade

API de login em Spring Boot com:

| Parte | O que tem | Onde |
|-------|-----------|------|
| **01 - Documentação** | OpenAPI + Swagger UI | `docs/OpenApiConfig.java`, anotações em `controller/UsuarioController.java` |
| **02 - Métricas** | Actuator + Micrometer + Prometheus + dashboard Grafana | `metrics/LoginMetrics.java`, `docker-compose.yml`, `prometheus/`, `grafana/` |
| **03 - Segurança** | Spring Security + `JdbcUserDetailsManager` + roles + bloqueio por tentativas | `security/SecurityConfig.java`, `service/LoginService.java` |

## Estrutura

```
MS-AV-02-Seguranca-Observabilidade/
├── pom.xml
├── docker-compose.yml              # Prometheus + Grafana
├── requests.http                   # requisições prontas (extensão REST Client)
├── prometheus/prometheus.yml
├── grafana/
│   ├── provisioning/               # datasource + dashboard carregados automaticamente
│   └── dashboards/login-dashboard.json
├── docs/                           # print + export do dashboard (entrega)
└── src/
    ├── main/java/br/espm/msav02/
    │   ├── MsAv02Application.java
    │   ├── docs/OpenApiConfig.java
    │   ├── security/SecurityConfig.java
    │   ├── metrics/LoginMetrics.java
    │   ├── controller/UsuarioController.java
    │   ├── service/{LoginService,UsuarioService}.java
    │   ├── repository/{UsuarioRepository,LoginAttemptRepository}.java
    │   └── dto/...
    ├── main/resources/{application.properties,schema.sql,data.sql}
    └── test/java/br/espm/msav02/FluxoCompletoTest.java
```

## Como rodar no VS Code

Pré-requisitos: JDK 17+, Maven e Docker. Extensões recomendadas (o VS Code sugere ao abrir a pasta): Java Pack, Spring Boot Pack, REST Client.

1. **File > Open Folder** e escolha `MS-AV-02-Seguranca-Observabilidade`.
2. Terminal: `mvn test` - roda o fluxo completo (login, bloqueio, unlock, roles, métricas, OpenAPI).
3. Suba a API: `mvn spring-boot:run` (ou abra `MsAv02Application.java` e clique em **Run**).
4. Em outro terminal: `docker compose up -d` (Prometheus e Grafana).

O banco é H2 em memória: cada reinício da aplicação volta ao estado inicial (usuários de teste abaixo, sem bloqueios).

## Passo 01 - Documentação dos endpoints

- Swagger UI: http://localhost:8080/swagger-ui.html
- JSON OpenAPI: http://localhost:8080/v3/api-docs
- Para testar endpoints protegidos no Swagger, clique em **Authorize** e informe usuário/senha (HTTP Basic).

## Passo 02 - Métricas

Indicadores (expostos em http://localhost:8080/actuator/prometheus):

| Métrica | Tipo | Significado |
|---------|------|-------------|
| `logins_validos_total` | counter | logins válidos |
| `logins_invalidos_total` | counter | logins inválidos (inclui tentativa em usuário bloqueado) |
| `usuarios_bloqueados` | gauge | usuários bloqueados agora (consulta `login_attempts`) |

- Prometheus: http://localhost:9090 (alvo `ms-av-02` deve estar **UP** em *Status > Targets*)
- Grafana: http://localhost:3000 (usuário `admin`, senha `admin`) - o dashboard **Logins - MS-AV-02** já aparece pronto em *Dashboards*.
- Gere tráfego com o `requests.http` (blocos 1 e 2) e veja os painéis mudarem em ~5 s.

## Passo 03 - Segurança

| Método | Endpoint | Acesso |
|--------|----------|--------|
| POST | `/login` | público |
| PUT | `/trocarsenha/{userid}` | público |
| POST | `/` (cadastra usuário) | `ROLE_MANUTENCAO` |
| GET | `/` (lista usuários) | `ROLE_MANUTENCAO` |
| PUT | `/unlock/{userid}` | `ROLE_ADMIN` |

Regras:
- `/login` devolve **200** (válido) ou **401** (inválido ou bloqueado).
- A **2ª falha seguida** bloqueia o usuário (`login_attempts.locked = true`). Login válido zera o contador.
- Usuário bloqueado não autentica nem no `/login` nem nos endpoints protegidos (para o Spring ele fica *disabled*).
- Usuários e roles são lidos pelo `JdbcUserDetailsManager` (tabelas `usuarios` e `authorities`). Usuários novos recebem `ROLE_USER`.
- Endpoints protegidos usam HTTP Basic.

Usuários de teste:

| Usuário | Senha | Roles |
|---------|-------|-------|
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_MANUTENCAO` |
| `manut` | `manut123` | `ROLE_MANUTENCAO` |
| `user` | `user123` | `ROLE_USER` |

## Entrega (checklist do enunciado)

1. Repositório **público** no GitHub:
   ```
   git init
   git add .
   git commit -m "MS-AV-02 - Seguranca e Observabilidade"
   git branch -M main
   git remote add origin https://github.com/<seu-usuario>/<seu-repo>.git
   git push -u origin main
   ```
2. Print do dashboard em `docs/dashboard-grafana.png`.
3. Dashboard exportado em `docs/dashboard-exportado.json` (Export > Export as code > Save to file).
4. Enviar o link do repositório.

## Observações

- O script SQL do enunciado tinha uma vírgula sobrando em `login_attempts` (erro de sintaxe); corrigida em `schema.sql`.
- `usuarios.senha` é `VARCHAR(10)`, então as senhas ficam em texto puro (`NoOpPasswordEncoder`). Em produção use BCrypt com coluna de pelo menos 60 caracteres.
- Se o `admin` for bloqueado (2 falhas no `/login`), reinicie a aplicação: o banco em memória volta ao início.
