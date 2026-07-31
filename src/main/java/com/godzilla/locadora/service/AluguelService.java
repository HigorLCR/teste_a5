package com.godzilla.locadora.service;

import com.godzilla.locadora.domain.Aluguel;
import com.godzilla.locadora.domain.Filme;
import com.godzilla.locadora.domain.Usuario;
import com.godzilla.locadora.dto.AluguelResponse;
import com.godzilla.locadora.dto.DevolucaoResponse;
import com.godzilla.locadora.exception.AluguelNaoPermitidoException;
import com.godzilla.locadora.exception.RecursoNaoEncontradoException;
import com.godzilla.locadora.repository.AluguelRepository;
import com.godzilla.locadora.repository.FilmeRepository;
import com.godzilla.locadora.repository.UsuarioRepository;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Registra o aluguel de um filme por um cliente. Tudo em uma transacao: se
     * qualquer etapa falhar, o decremento do estoque volta atras junto.
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

        // Mensagem clara no caso comum; a garantia da regra e a constraint, tratada abaixo.
        if (aluguelRepository.existsByUsuarioIdAndDevolvidoEmIsNull(usuarioId)) {
            throw new AluguelNaoPermitidoException(
                    "Cliente ja possui um filme alugado. E permitido um por vez.");
        }

        if (filmeRepository.reservarUmaUnidade(filmeId) == 0) {
            throw new AluguelNaoPermitidoException(
                    "Filme '%s' sem estoque disponivel.".formatted(filme.getTitulo()));
        }

        // saveAndFlush, e nao save: forca o INSERT a ir ao banco dentro deste try,
        // para que a violacao do indice unico caia no catch e nao no commit.
        try {
            Aluguel aluguel = aluguelRepository.saveAndFlush(new Aluguel(filme, usuario));
            return AluguelResponse.de(aluguel);

        } catch (DataIntegrityViolationException e) {
            throw new AluguelNaoPermitidoException(
                    "Cliente ja possui um filme alugado. E permitido um por vez.");
        }
    }

    /**
     * Operacao inversa de {@link #alugar(Long, Long)}: fecha o aluguel e repoe a
     * unidade no estoque, na mesma transacao.
     *
     * <p>Nao recebe id de aluguel — o cliente tem no maximo um em aberto, entao
     * ele proprio identifica o registro, e ninguem consegue devolver o filme de
     * outro.
     *
     * @throws RecursoNaoEncontradoException se o cliente nao existir ou nao
     *                                       estiver com nenhum filme
     */
    @Transactional
    public DevolucaoResponse devolver(Long usuarioId) {

        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente %d nao encontrado".formatted(usuarioId)));

        Aluguel aluguel = aluguelRepository.buscarEmAbertoDoUsuario(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente %d nao possui filme alugado.".formatted(usuarioId)));

        Instant devolvidoEm = Instant.now();

        // Perdedor de uma devolucao concorrente: nao repoe estoque.
        if (aluguelRepository.registrarDevolucao(aluguel.getId(), devolvidoEm) == 0) {
            throw new RecursoNaoEncontradoException(
                    "Cliente %d nao possui filme alugado.".formatted(usuarioId));
        }

        filmeRepository.devolverUmaUnidade(aluguel.getFilme().getId());

        return DevolucaoResponse.de(aluguel, devolvidoEm);
    }
}
