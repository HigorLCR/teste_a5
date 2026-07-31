package com.godzilla.locadora.service;

import com.godzilla.locadora.domain.Aluguel;
import com.godzilla.locadora.domain.Filme;
import com.godzilla.locadora.domain.Usuario;
import com.godzilla.locadora.dto.AluguelResponse;
import com.godzilla.locadora.exception.AluguelNaoPermitidoException;
import com.godzilla.locadora.exception.RecursoNaoEncontradoException;
import com.godzilla.locadora.repository.AluguelRepository;
import com.godzilla.locadora.repository.FilmeRepository;
import com.godzilla.locadora.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de aluguel.
 *
 * <p>Duas restricoes do enunciado convergem aqui:
 * <ul>
 *   <li>"permite o aluguel de um filme somente se a locadora possuir este filme
 *       em estoque";</li>
 *   <li>"so e permitido que um cliente alugue um filme de cada vez".</li>
 * </ul>
 *
 * <p>Ambas sao condicoes de corrida classicas: verificar e depois agir deixa uma
 * janela entre as duas operacoes. A estrategia adotada nao depende dessa janela
 * ser pequena — ela elimina a janela.
 */
@Service
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;

    public AluguelService(AluguelRepository aluguelRepository,
                          FilmeRepository filmeRepository,
                          UsuarioRepository usuarioRepository) {
        this.aluguelRepository = aluguelRepository;
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra o aluguel de um filme por um cliente.
     *
     * <p>Tudo roda dentro de uma unica transacao: se qualquer etapa falhar, o
     * decremento do estoque volta atras junto. Nao existe estado intermediario
     * visivel em que o estoque caiu mas o aluguel nao foi registrado.
     *
     * @throws RecursoNaoEncontradoException se o filme ou o cliente nao existir
     * @throws AluguelNaoPermitidoException  se nao houver estoque ou o cliente
     *                                       ja estiver com um filme
     */
    @Transactional
    public AluguelResponse alugar(Long filmeId, Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente %d nao encontrado".formatted(usuarioId)));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Filme %d nao encontrado".formatted(filmeId)));

        // ---------------------------------------------------------------------
        // 1) Cliente ja esta com um filme?
        // ---------------------------------------------------------------------
        // Esta verificacao existe para produzir uma mensagem clara no caso comum.
        // Ela NAO e a garantia da regra: entre este if e o insert la embaixo
        // existe uma janela. A garantia esta no passo 3.
        if (aluguelRepository.existsByUsuarioIdAndDevolvidoEmIsNull(usuarioId)) {
            throw new AluguelNaoPermitidoException(
                    "Cliente ja possui um filme alugado. E permitido um por vez.");
        }

        // ---------------------------------------------------------------------
        // 2) Reserva atomica do estoque
        // ---------------------------------------------------------------------
        // Um unico UPDATE ... WHERE estoque > 0 decide e aplica ao mesmo tempo,
        // sob o lock de linha do banco. Retorno 0 significa que nao havia
        // estoque no instante exato da operacao — sem chance de duas requisicoes
        // levarem a mesma ultima copia.
        if (filmeRepository.reservarUmaUnidade(filmeId) == 0) {
            throw new AluguelNaoPermitidoException(
                    "Filme '%s' sem estoque disponivel.".formatted(filme.getTitulo()));
        }

        // ---------------------------------------------------------------------
        // 3) Registro do aluguel — aqui mora a garantia de unicidade
        // ---------------------------------------------------------------------
        // saveAndFlush, e nao save: force o INSERT a ir ao banco AGORA, dentro
        // deste try. Com save(), a escrita poderia ser adiada ate o commit, ou
        // seja, fora do alcance deste catch.
        //
        // Se outra requisicao do mesmo cliente venceu a corrida, o indice unico
        // parcial uk_aluguel_ativo_por_usuario rejeita este insert. Traduzimos a
        // violacao para a mesma regra de negocio do passo 1 — e o rollback da
        // transacao devolve a unidade de estoque reservada no passo 2.
        try {
            Aluguel aluguel = aluguelRepository.saveAndFlush(new Aluguel(filme, usuario));
            return AluguelResponse.de(aluguel);

        } catch (DataIntegrityViolationException e) {
            throw new AluguelNaoPermitidoException(
                    "Cliente ja possui um filme alugado. E permitido um por vez.");
        }
    }
}
