package com.godzilla.locadora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Testes das regras de aluguel.
 *
 * <p>Sao testes UNITARIOS: nenhum contexto Spring sobe, nenhum banco e usado, os
 * repositorios sao dubles. Rodam em milissegundos e falham por um motivo so — a
 * regra de negocio. Isso e possivel porque {@code AluguelService} recebe suas
 * dependencias pelo construtor; com {@code @Autowired} em campo, seria preciso
 * subir o contexto inteiro para testar um {@code if}.
 */
class AluguelServiceTest {

    private AluguelRepository aluguelRepository;
    private FilmeRepository filmeRepository;
    private UsuarioRepository usuarioRepository;
    private AluguelService aluguelService;

    @BeforeEach
    void preparar() {
        aluguelRepository = mock(AluguelRepository.class);
        filmeRepository = mock(FilmeRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        aluguelService = new AluguelService(aluguelRepository, filmeRepository, usuarioRepository);
    }

    @Test
    @DisplayName("aluga quando ha estoque e o cliente esta livre")
    void deveAlugarQuandoHaEstoqueEClienteLivre() {
        Usuario usuario = usuario(1L);
        Filme filme = filme(10L, "Godzilla", 3);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(aluguelRepository.existsByUsuarioIdAndDevolvidoEmIsNull(1L)).thenReturn(false);
        when(filmeRepository.reservarUmaUnidade(10L)).thenReturn(1);
        when(aluguelRepository.saveAndFlush(any(Aluguel.class))).thenAnswer(invocacao -> {
            Aluguel salvo = invocacao.getArgument(0);
            salvo.setId(99L);
            return salvo;
        });

        AluguelResponse resposta = aluguelService.alugar(10L, 1L);

        assertThat(resposta.aluguelId()).isEqualTo(99L);
        assertThat(resposta.filmeId()).isEqualTo(10L);
        assertThat(resposta.titulo()).isEqualTo("Godzilla");
        assertThat(resposta.usuarioId()).isEqualTo(1L);
        assertThat(resposta.alugadoEm()).isNotNull();
    }

    @Test
    @DisplayName("recusa quando o cliente ja possui um filme e nao consome estoque")
    void deveRecusarQuandoClienteJaPossuiFilme() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme(10L, "Godzilla", 3)));
        when(aluguelRepository.existsByUsuarioIdAndDevolvidoEmIsNull(1L)).thenReturn(true);

        assertThatThrownBy(() -> aluguelService.alugar(10L, 1L))
                .isInstanceOf(AluguelNaoPermitidoException.class)
                .hasMessageContaining("um por vez");

        // A verificacao mais importante deste teste: um cliente barrado nao pode
        // consumir uma unidade do estoque no caminho.
        verify(filmeRepository, never()).reservarUmaUnidade(anyLong());
        verify(aluguelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("recusa quando nao ha estoque e nao registra aluguel")
    void deveRecusarQuandoNaoHaEstoque() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme(10L, "Godzilla", 0)));
        when(aluguelRepository.existsByUsuarioIdAndDevolvidoEmIsNull(1L)).thenReturn(false);
        // Zero linhas afetadas = o UPDATE condicional nao encontrou estoque.
        when(filmeRepository.reservarUmaUnidade(10L)).thenReturn(0);

        assertThatThrownBy(() -> aluguelService.alugar(10L, 1L))
                .isInstanceOf(AluguelNaoPermitidoException.class)
                .hasMessageContaining("sem estoque");

        verify(aluguelRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("traduz violacao do indice unico para regra de negocio")
    void deveTraduzirViolacaoDeUnicidade() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme(10L, "Godzilla", 3)));
        when(aluguelRepository.existsByUsuarioIdAndDevolvidoEmIsNull(1L)).thenReturn(false);
        when(filmeRepository.reservarUmaUnidade(10L)).thenReturn(1);
        // Cenario de corrida: outra requisicao do mesmo cliente inseriu primeiro,
        // e o indice unico parcial rejeitou este insert.
        when(aluguelRepository.saveAndFlush(any(Aluguel.class)))
                .thenThrow(new DataIntegrityViolationException("uk_aluguel_ativo_por_usuario"));

        assertThatThrownBy(() -> aluguelService.alugar(10L, 1L))
                .isInstanceOf(AluguelNaoPermitidoException.class)
                .hasMessageContaining("um por vez");
    }

    @Test
    @DisplayName("404 quando o filme nao existe")
    void deveFalharQuandoFilmeNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(filmeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aluguelService.alugar(999L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Filme 999");
    }

    @Test
    @DisplayName("404 quando o cliente nao existe")
    void deveFalharQuandoClienteNaoExiste() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aluguelService.alugar(10L, 404L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cliente 404");
    }

    // -------------------------------------------------------------- devolucao

    @Test
    @DisplayName("devolve o filme, fecha o aluguel e repoe o estoque")
    void deveDevolverFilme() {
        Usuario usuario = usuario(1L);
        Filme filme = filme(10L, "Godzilla", 2);
        Aluguel aluguel = aluguel(77L, filme, usuario);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(aluguelRepository.buscarEmAbertoDoUsuario(1L)).thenReturn(Optional.of(aluguel));
        when(aluguelRepository.registrarDevolucao(eq(77L), any(Instant.class))).thenReturn(1);

        DevolucaoResponse resposta = aluguelService.devolver(1L);

        assertThat(resposta.aluguelId()).isEqualTo(77L);
        assertThat(resposta.filmeId()).isEqualTo(10L);
        assertThat(resposta.titulo()).isEqualTo("Godzilla");
        assertThat(resposta.usuarioId()).isEqualTo(1L);
        assertThat(resposta.devolvidoEm()).isNotNull();
        assertThat(resposta.alugadoEm()).isNotNull();
        verify(filmeRepository).devolverUmaUnidade(10L);
    }

    @Test
    @DisplayName("404 quando o cliente nao esta com nenhum filme")
    void deveFalharQuandoNaoHaAluguelEmAberto() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(aluguelRepository.buscarEmAbertoDoUsuario(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aluguelService.devolver(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("nao possui filme alugado");

        verify(filmeRepository, never()).devolverUmaUnidade(anyLong());
    }

    @Test
    @DisplayName("devolucao concorrente nao credita estoque duas vezes")
    void deveIgnorarDevolucaoJaRegistrada() {
        Usuario usuario = usuario(1L);
        Filme filme = filme(10L, "Godzilla", 2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(aluguelRepository.buscarEmAbertoDoUsuario(1L))
                .thenReturn(Optional.of(aluguel(77L, filme, usuario)));
        // Zero linhas afetadas = outra requisicao fechou este aluguel primeiro.
        when(aluguelRepository.registrarDevolucao(eq(77L), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> aluguelService.devolver(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("nao possui filme alugado");

        // O perdedor da corrida NAO repoe estoque.
        verify(filmeRepository, never()).devolverUmaUnidade(anyLong());
    }

    @Test
    @DisplayName("404 ao devolver com cliente inexistente")
    void deveFalharAoDevolverComClienteInexistente() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aluguelService.devolver(404L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cliente 404");

        verify(aluguelRepository, never()).registrarDevolucao(anyLong(), any());
    }

    // ------------------------------------------------------------------ apoio

    private Aluguel aluguel(Long id, Filme filme, Usuario usuario) {
        Aluguel aluguel = new Aluguel(filme, usuario);
        aluguel.setId(id);
        return aluguel;
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario("Cliente", "cliente@teste.com", "hash");
        usuario.setId(id);
        return usuario;
    }

    private Filme filme(Long id, String titulo, int estoque) {
        Filme filme = new Filme(titulo, "Ishiro Honda", (short) 1954, estoque);
        filme.setId(id);
        return filme;
    }
}
