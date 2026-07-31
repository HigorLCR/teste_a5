# API — endpoints

A aplicação sobe na porta **8089**.

| Método | Caminho | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/usuarios/usuario` | pública | Cadastra um cliente e devolve o token |
| `POST` | `/usuarios/login` | pública | Autentica e devolve o token |
| `GET` | `/locadora/godzilla` | pública | Busca filmes por título parcial e/ou ano |
| `POST` | `/godzilla` | **Bearer** | Aluga um filme para o cliente autenticado |

---

## Teste 3 — Cadastro e autenticação

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

## Teste 2 — Busca no catálogo

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

## Teste 1 — Aluguel

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

---

## Respostas de erro

Todos os erros seguem a RFC 9457 (`application/problem+json`):

```json
{
  "title": "Aluguel não permitido",
  "status": 403,
  "detail": "Filme 'Invasao dos Astro-Monstros' sem estoque disponivel.",
  "instance": "/godzilla"
}
```

Erros de validação detalham campo a campo:

```json
{
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos da requisição são inválidos.",
  "instance": "/usuarios/usuario",
  "campos": { "email": "email inválido", "senha": "senha deve ter entre 4 e 72 caracteres" }
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
