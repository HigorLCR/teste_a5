# Godzilla Local Filmes — API REST

API de locadora especializada na saga Gojira, em que cada cliente pode manter
apenas um filme alugado por vez.

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

Não há passo manual de banco: a dependência `spring-boot-docker-compose` lê o
`compose.yaml`, sobe o container do PostgreSQL, aguarda o healthcheck e injeta as
credenciais na aplicação. O Flyway então cria o schema e carrega o catálogo de
28 filmes.

> A **primeira execução** baixa as dependências do Maven e a imagem do Postgres —
> pode levar alguns minutos. As seguintes sobem em segundos.

**3. Confirme que está no ar**

```bash
curl http://localhost:8089/actuator/health
# {"status":"UP","components":{"db":{"status":"UP", ...
```

O `db: UP` indica que a aplicação conectou ao banco com sucesso.

Para parar: `Ctrl+C`. O container do Postgres continua de pé e os dados persistem
em um volume nomeado; para removê-lo, `docker compose down`.

## Como executar os testes

```bash
./mvnw test          # Linux / macOS
.\mvnw.cmd test      # Windows
```

São 16 testes: 15 unitários dos serviços e 1 de integração que sobe um
PostgreSQL real via Testcontainers — por isso o Docker também é necessário para
rodar a suíte.

---

## Documentação

| Documento | Conteúdo |
|---|---|
| [docs/api.md](docs/api.md) | Endpoints, exemplos de requisição e códigos de resposta |
| [docs/questoes.md](docs/questoes.md) | Teste funcional e teste de banco de dados |
| [docs/decisoes-tecnicas.md](docs/decisoes-tecnicas.md) | Decisões de implementação e divergências do enunciado |
