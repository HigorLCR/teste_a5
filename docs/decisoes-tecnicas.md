# Decisões técnicas

Registro das escolhas de implementação da API e do porquê de cada uma.

## A unicidade do aluguel é garantida pelo banco, não por um `if`

O requisito "apenas 1 registro por aluguel" e "um filme de cada vez" é uma
condição de corrida clássica. Verificar com um `SELECT` e depois inserir deixa
uma janela: duas requisições simultâneas do mesmo cliente passam pela verificação
juntas, antes de qualquer uma gravar. A garantia está em um **índice único
parcial**:

```sql
CREATE UNIQUE INDEX uk_aluguel_ativo_por_usuario
    ON aluguel (usuario_id)
    WHERE devolvido_em IS NULL;
```

A restrição vale apenas para as linhas de alugueis em aberto, então o cliente
pode ter histórico ilimitado, mas nunca dois ativos. O `SELECT` prévio continua
existindo no serviço, mas só para produzir uma mensagem clara no caso comum — a
violação da constraint é capturada e traduzida para a mesma regra de negócio.

## O estoque é decrementado com um `UPDATE` condicional atômico

```sql
UPDATE filme SET estoque = estoque - 1 WHERE id = ? AND estoque > 0
```

Ler o estoque, testar em Java e gravar de volta permitiria que dois clientes
levassem a última cópia. Aqui a decisão e a escrita acontecem no mesmo comando,
sob o lock de linha do banco: o retorno é 1 (reservou) ou 0 (não havia), sem
terceiro resultado e sem janela. Uma restrição `CHECK (estoque >= 0)` fecha a
última brecha.

**Verificado empiricamente:** 10 requisições simultâneas disputando 1 cópia
resultaram em exatamente 1 sucesso e 9 recusas, em três rodadas consecutivas; com
3 cópias, exatamente 3 sucessos.

## A identidade de quem aluga vem do token, não do corpo

O `POST /godzilla` não aceita `usuarioId` no payload. O cliente é lido do
`subject` do JWT, já validado pelo Spring Security. Aceitar o id no corpo
permitiria alugar em nome de outra pessoa trocando um número.

## JWT pelo próprio Spring Security

A emissão e validação usam `NimbusJwtEncoder`/`NimbusJwtDecoder` do
`spring-security-oauth2-resource-server`, em vez de uma biblioteca de terceiros.
Menos dependências, e nenhum filtro de autenticação escrito à mão — que é
justamente onde erros de segurança costumam nascer.

Senhas são armazenadas com **BCrypt**, algoritmo deliberadamente lento e com
salt aleatório embutido. A coluna se chama `senha_hash` para deixar explícito, a
quem ler o schema, que não há senha recuperável.

## DTOs separados das entidades

Nenhuma entidade JPA é serializada diretamente. Isso desacopla o contrato público
da API do schema do banco e torna impossível, por construção, vazar o hash da
senha em uma resposta.

## O schema pertence ao Flyway

`ddl-auto: validate` — o Hibernate apenas confere se as entidades batem com as
tabelas, sem nunca alterar o schema. Toda mudança estrutural é um arquivo SQL
versionado em `src/main/resources/db/migration`, o que dá histórico e garante que
qualquer ambiente chegue ao mesmo estado.

## Separação em camadas

A estrutura segue o padrão MVC: o **controller** só traduz HTTP, o **service**
concentra a regra de negócio e desconhece HTTP, o **repository** cuida do acesso
a dados. É essa fronteira que permite testar toda a regra de negócio sem subir o
Spring nem o banco.

```
src/main/java/com/godzilla/locadora/
├── LocadoraApplication.java
├── config/        SecurityConfig
├── controller/    FilmeController · AluguelController · UsuarioController
├── service/       FilmeService · AluguelService · UsuarioService · TokenService
├── repository/    FilmeRepository · AluguelRepository · UsuarioRepository
├── domain/        Filme · Aluguel · Usuario
├── dto/           requests e responses (records imutáveis)
└── exception/     exceções de domínio + TratadorGlobalDeErros
```

> No Spring Boot 4 os starters foram renomeados: a dependência web é
> `spring-boot-starter-webmvc` (não `spring-boot-starter-web`) e os starters de
> teste são separados por módulo — divergência frequente em relação a material
> de referência escrito para a versão 3.

## Divergências do enunciado, e como foram tratadas

| Ponto do enunciado | Decisão |
|---|---|
| Caminho grafado `localdora/godzilla` | Provável erro de digitação de "locadora". O endpoint responde **nos dois caminhos**, para atender tanto a grafia literal quanto a correta |
| Caminho `usuários/usuário` com acentos | Adotado `/usuarios/usuario`. URLs acentuadas exigem codificação percentual e são fonte comum de erro entre clientes |
| `403 Forbidden` para falta de estoque | Semanticamente, `409 Conflict` seria mais preciso — `403` trata de autorização, não de estado do recurso. O requisito é explícito, então **o enunciado prevaleceu** |
| Busca "por título **ou ano**", mas o JSON de exemplo não tem ano | O campo `ano` foi incluído na entidade e na resposta, para atender o texto do requisito |
| JSON de exemplo alterna `filmesId` e `filmeId` | Padronizado como `filmeId`, no singular |
| Texto pede cadastro, mas o exemplo mostra payload de login | Implementados **dois endpoints** — `/usuarios/usuario` e `/usuarios/login` — ambos devolvendo o mesmo formato com token |
