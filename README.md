# Godzilla Local Filmes — API REST

API de locadora especializada na saga Gojira, em que cada cliente pode manter
**apenas um filme alugado por vez**. Implementa os três testes do desafio:
consulta ao catálogo, aluguel com controle de estoque, e cadastro/autenticação
de clientes com JWT.

Java 21 · Spring Boot 4.1 · PostgreSQL 17 · Flyway · JWT · Maven Wrapper

---

## Pré-requisitos

- **JDK 21** ou superior — verifique com `java -version`
- **Docker** em execução — o PostgreSQL sobe em container automaticamente

Não é necessário instalar Maven nem PostgreSQL: o Maven Wrapper (`mvnw`) está
versionado no repositório e o banco roda em container.

## Como rodar

**1. Clone o repositório**

```bash
git clone <url-do-repositorio>
cd teste_a5
```

**2. Suba a aplicação**

```bash
./mvnw spring-boot:run       # Linux / macOS
.\mvnw.cmd spring-boot:run   # Windows (PowerShell)
```

É só isso — não há passo manual de banco. A dependência `spring-boot-docker-compose`
lê o `compose.yaml`, sobe o container do PostgreSQL, aguarda o healthcheck e
injeta as credenciais na aplicação. O Flyway então cria o schema e carrega o
catálogo de 28 filmes.

> **A primeira execução** baixa as dependências do Maven e a imagem do Postgres —
> pode levar alguns minutos. As seguintes sobem em segundos.

**3. Confirme que está no ar**

```bash
curl http://localhost:8089/actuator/health
# {"status":"UP","components":{"db":{"status":"UP", ...
```

O `db: UP` indica que a aplicação conectou ao banco com sucesso.

Para parar: `Ctrl+C`. O container do Postgres continua de pé (os dados persistem
em um volume nomeado); para removê-lo, `docker compose down`.

## Como executar os testes

```bash
./mvnw test          # Linux / macOS
.\mvnw.cmd test      # Windows
```

São 16 testes: 15 unitários dos serviços (com dublês, sem banco) e 1 de
integração que sobe um PostgreSQL real via Testcontainers — por isso o Docker
também é necessário para rodar a suíte.

---

## Endpoints

A aplicação sobe na porta **8089**.

| Método | Caminho | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/usuarios/usuario` | pública | Cadastra um cliente e devolve o token |
| `POST` | `/usuarios/login` | pública | Autentica e devolve o token |
| `GET` | `/locadora/godzilla` | pública | Busca filmes por título parcial e/ou ano |
| `POST` | `/godzilla` | **Bearer** | Aluga um filme para o cliente autenticado |

### Teste 3 — Cadastro e autenticação

```bash
curl -X POST http://localhost:8089/usuarios/usuario \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Cliente","email":"cliente@cliente.com","senha":"123@"}'
```

```json
{
  "auth": true,
  "usuario": { "id": 1, "email": "cliente@cliente.com", "nome": "Cliente" },
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsb2NhZG9yYS1nb2R6aWxsYSIsInN1YiI6IjEi..."
}
```

Login de um cliente já cadastrado, mesmo formato de resposta:

```bash
curl -X POST http://localhost:8089/usuarios/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"cliente@cliente.com","senha":"123@"}'
```

| Situação | Status |
|---|---|
| Cadastro realizado | `201 Created` |
| E-mail já cadastrado | `409 Conflict` |
| Campos inválidos | `400 Bad Request` |
| Login bem-sucedido | `200 OK` |
| E-mail ou senha incorretos | `401 Unauthorized` |

### Teste 2 — Busca no catálogo

Não é necessário informar o título inteiro; a busca retorna todos os filmes que
o contenham, ignorando maiúsculas.

```bash
curl 'http://localhost:8089/locadora/godzilla?titulo=mechagodzilla'
curl 'http://localhost:8089/locadora/godzilla?ano=1964'
curl 'http://localhost:8089/locadora/godzilla?titulo=godzilla&ano=1992'
curl 'http://localhost:8089/locadora/godzilla'          # catálogo completo
```

```json
{
  "filmes": [
    { "filmeId": 12, "titulo": "Godzilla vs. Mechagodzilla", "diretor": "Jun Fukuda", "ano": 1974, "estoque": 3 },
    { "filmeId": 13, "titulo": "O Terror de Mechagodzilla", "diretor": "Ishiro Honda", "ano": 1975, "estoque": 1 }
  ]
}
```

### Teste 1 — Aluguel

Requer o token obtido no cadastro ou login:

```bash
TOKEN='<cole-o-token-aqui>'

curl -X POST http://localhost:8089/godzilla \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"filmeId":1}'
```

```json
{
  "aluguelId": 1,
  "filmeId": 1,
  "titulo": "Godzilla",
  "usuarioId": 1,
  "alugadoEm": "2026-07-31T02:25:35.178Z"
}
```

| Situação | Status |
|---|---|
| Aluguel permitido | `200 OK` |
| Filme sem estoque | `403 Forbidden` |
| Cliente já possui um filme | `403 Forbidden` |
| Filme inexistente | `404 Not Found` |
| Sem token, ou token inválido/expirado | `401 Unauthorized` |

O catálogo inclui dois títulos com estoque zero — *Invasão dos Astro-Monstros*
(`filmeId` 6) e *Godzilla vs. Megalon* (`filmeId` 11) — para exercitar o `403`
sem precisar esgotar o estoque antes.

### Respostas de erro

Todos os erros seguem a RFC 9457 (`application/problem+json`):

```json
{
  "title": "Aluguel não permitido",
  "status": 403,
  "detail": "Filme 'Invasao dos Astro-Monstros' sem estoque disponivel.",
  "instance": "/godzilla"
}
```

---

## Configuração

Variáveis de ambiente reconhecidas (todas têm padrão de desenvolvimento):

| Variável | Padrão | Descrição |
|---|---|---|
| `SERVER_PORT` | `8089` | Porta HTTP da aplicação |
| `JWT_SEGREDO` | chave de desenvolvimento | Chave HMAC de assinatura. **Obrigatória em produção** — quem a possui consegue forjar tokens |
| `JWT_DURACAO_MINUTOS` | `120` | Validade do token |

Nenhum segredo de produção está versionado neste repositório.

---

## Documentação complementar

| Documento | Conteúdo |
|---|---|
| [docs/questoes_1_2.md](docs/questoes_1_2.md) | Teste funcional: mesa de teste do algoritmo e fluxograma da situação do aluno |
| [docs/decisoes-tecnicas.md](docs/decisoes-tecnicas.md) | Por que a unicidade está no banco, como o estoque é decrementado, e as divergências do enunciado |
