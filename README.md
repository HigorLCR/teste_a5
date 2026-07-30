# teste_a5

## Documentação

| Documento | Conteúdo |
|---|---|
| [docs/01-teste-funcional.md](docs/01-teste-funcional.md) | **Parte 1** — mesa de teste do algoritmo e fluxograma da situação do aluno |
| [docs/02-banco-de-dados.md](docs/02-banco-de-dados.md) | **Parte 2** — comandos de INSERT/UPDATE em `Pessoa` e consulta do endereço completo |

## Estrutura do projeto

```
teste_a5/
├── pom.xml
├── compose.yaml
├── mvnw · mvnw.cmd · .mvn/
├── docs/
│   ├── 01-teste-funcional.md
│   └── 02-banco-de-dados.md
├── src/
│   ├── main/
│   │   ├── java/com/godzilla/locadora/
│   │   │   └── LocadoraApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/migration/
│   └── test/java/com/godzilla/locadora/
│       ├── LocadoraApplicationTests.java
│       ├── TestcontainersConfiguration.java
│       └── TestLocadoraApplication.java
├── .gitignore · .gitattributes
└── HELP.md
```

| Arquivo | Para que serve |
|---|---|
| `pom.xml` | A "receita" do projeto: dependências, Java 21, plugins |
| `mvnw.cmd`, `mvnw`, `.mvn/` | Maven Wrapper — roda Maven sem precisar instalar Maven |
| `compose.yaml` | Define o container do Postgres |
| `src/main/java/.../LocadoraApplication.java` | **Ponto de entrada.** O `@SpringBootApplication` liga a autoconfiguração, varre o pacote procurando os componentes (`@RestController`, `@Service`…) e registra configurações |
| `src/main/resources/application.yaml` | Configuração da aplicação |
| `src/main/resources/db/migration/` | Migrações Flyway (`V1__criar_tabelas.sql`) |
| `src/test/.../LocadoraApplicationTests.java` | Teste que verifica se o contexto do Spring sobe |
| `src/test/.../TestcontainersConfiguration.java` | Sobe um Postgres real em container durante os testes |
| `src/test/.../TestLocadoraApplication.java` | Atalho para rodar a aplicação usando esse Postgres de teste |
| `static/`, `templates/`, `HELP.md` | Gerados pelo Spring Initializr e não utilizados (API pura, sem HTML) |

> No Spring Boot 4 os starters foram renomeados e modularizados: a dependência web é
> `spring-boot-starter-webmvc` (não `spring-boot-starter-web`), e os starters de teste são
> separados por módulo (`spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`…)
> em vez do antigo `spring-boot-starter-test` único.
