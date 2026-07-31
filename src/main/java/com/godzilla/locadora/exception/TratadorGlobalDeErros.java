package com.godzilla.locadora.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traducao centralizada de excecoes para respostas HTTP.
 *
 * <p>Este e o unico lugar do projeto que decide qual status corresponde a qual
 * falha. As excecoes de dominio nao carregam mais {@code @ResponseStatus}: elas
 * descrevem o que aconteceu em vocabulario de negocio, e a camada web — aqui —
 * decide como isso se traduz em HTTP. Se amanha a locadora expuser a mesma regra
 * por uma fila ou uma CLI, o dominio nao precisa mudar.
 *
 * <p>As respostas usam {@link ProblemDetail}, a implementacao da RFC 9457
 * (Problem Details for HTTP APIs) que o Spring traz nativamente. O corpo sai com
 * content-type {@code application/problem+json} e formato previsivel, em vez do
 * JSON improvisado do handler padrao — que, em desenvolvimento, ainda vinha com
 * stack trace dentro.
 */
@RestControllerAdvice
public class TratadorGlobalDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    @ExceptionHandler(AluguelNaoPermitidoException.class)
    public ProblemDetail tratarAluguelNaoPermitido(AluguelNaoPermitidoException e) {
        return problema(HttpStatus.FORBIDDEN, "Aluguel não permitido", e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarRecursoNaoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", e.getMessage());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ProblemDetail tratarEmailJaCadastrado(EmailJaCadastradoException e) {
        return problema(HttpStatus.CONFLICT, "E-mail já cadastrado", e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail tratarCredenciaisInvalidas(CredenciaisInvalidasException e) {
        return problema(HttpStatus.UNAUTHORIZED, "Falha na autenticação", e.getMessage());
    }

    /**
     * Falha de Bean Validation: devolve 400 com o detalhamento campo a campo,
     * para que o cliente saiba exatamente o que corrigir.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST,
                "Dados inválidos", "Um ou mais campos da requisição são inválidos.");
        problema.setProperty("campos", campos);
        return problema;
    }

    /** JSON malformado ou tipo incompativel no corpo da requisicao. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail tratarCorpoIlegivel(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição malformada",
                "O corpo da requisição não pôde ser lido. Verifique se é um JSON válido.");
    }

    /**
     * Rede de seguranca para qualquer falha nao prevista.
     *
     * <p>A causa real vai para o log, onde a equipe consegue investigar; o cliente
     * recebe apenas uma mensagem generica. Detalhe interno em resposta de erro —
     * stack trace, nome de classe, SQL — e informacao util para quem esta
     * atacando a aplicacao.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception e) {
        log.error("Erro nao tratado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado ao processar a requisição.");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
