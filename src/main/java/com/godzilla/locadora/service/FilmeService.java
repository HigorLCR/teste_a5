package com.godzilla.locadora.service;

import com.godzilla.locadora.dto.FilmeResponse;
import com.godzilla.locadora.repository.FilmeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilmeService {

    private final FilmeRepository filmeRepository;

    public FilmeService(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    /**
     * Busca filmes cujo titulo contenha o termo, opcionalmente filtrando por ano.
     * Termo ausente ou em branco significa sem filtro de titulo.
     */
    @Transactional(readOnly = true)
    public List<FilmeResponse> buscar(String titulo, Short ano) {
        String termo = (titulo == null) ? "" : titulo.trim();

        var encontrados = (ano == null)
                ? filmeRepository.buscarPorTitulo(termo)
                : filmeRepository.buscarPorTituloEAno(termo, ano);

        return encontrados.stream()
                .map(FilmeResponse::de)
                .toList();
    }
}
