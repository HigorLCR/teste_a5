package com.godzilla.locadora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.godzilla.locadora.domain.Filme;
import com.godzilla.locadora.dto.FilmeResponse;
import com.godzilla.locadora.repository.FilmeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FilmeServiceTest {

    private FilmeRepository filmeRepository;
    private FilmeService filmeService;

    @BeforeEach
    void preparar() {
        filmeRepository = mock(FilmeRepository.class);
        filmeService = new FilmeService(filmeRepository);
    }

    @Test
    @DisplayName("busca so por titulo quando o ano nao e informado")
    void deveBuscarSoPorTituloQuandoAnoAusente() {
        when(filmeRepository.buscarPorTitulo("godzilla"))
                .thenReturn(List.of(filme(1L, "Godzilla", (short) 1954, 3)));

        List<FilmeResponse> resultado = filmeService.buscar("godzilla", null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Godzilla");
        verify(filmeRepository, never()).buscarPorTituloEAno(anyString(), any());
    }

    @Test
    @DisplayName("combina titulo e ano quando ambos sao informados")
    void deveCombinarTituloEAno() {
        when(filmeRepository.buscarPorTituloEAno("mothra", (short) 1992))
                .thenReturn(List.of(filme(17L, "Godzilla vs. Mothra", (short) 1992, 3)));

        List<FilmeResponse> resultado = filmeService.buscar("mothra", (short) 1992);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).ano()).isEqualTo((short) 1992);
        verify(filmeRepository, never()).buscarPorTitulo(anyString());
    }

    @Test
    @DisplayName("titulo nulo ou em branco vira busca sem filtro")
    void deveTratarTituloAusenteComoBuscaSemFiltro() {
        when(filmeRepository.buscarPorTitulo("")).thenReturn(List.of());

        filmeService.buscar(null, null);
        filmeService.buscar("   ", null);

        ArgumentCaptor<String> termo = ArgumentCaptor.forClass(String.class);
        verify(filmeRepository, times(2)).buscarPorTitulo(termo.capture());
        assertThat(termo.getAllValues()).containsExactly("", "");
    }

    @Test
    @DisplayName("converte a entidade para DTO sem perder campos")
    void deveConverterEntidadeParaDto() {
        when(filmeRepository.buscarPorTitulo("shin"))
                .thenReturn(List.of(filme(27L, "Shin Godzilla", (short) 2016, 5)));

        FilmeResponse resposta = filmeService.buscar("shin", null).get(0);

        assertThat(resposta.filmeId()).isEqualTo(27L);
        assertThat(resposta.titulo()).isEqualTo("Shin Godzilla");
        assertThat(resposta.diretor()).isEqualTo("Ishiro Honda");
        assertThat(resposta.ano()).isEqualTo((short) 2016);
        assertThat(resposta.estoque()).isEqualTo(5);
    }

    private Filme filme(Long id, String titulo, Short ano, int estoque) {
        Filme filme = new Filme(titulo, "Ishiro Honda", ano, estoque);
        filme.setId(id);
        return filme;
    }
}
