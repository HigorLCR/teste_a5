package com.godzilla.locadora.service;

import com.godzilla.locadora.dto.FilmeResponse;
import com.godzilla.locadora.repository.FilmeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de consulta ao catalogo.
 *
 * <p>A camada de servico nao conhece HTTP: nao recebe request, nao devolve
 * status code, nao lanca nada especifico de web. Isso e o que permite testa-la
 * com JUnit puro e reaproveita-la em outro tipo de entrada (uma CLI, uma fila).
 */
@Service
public class FilmeService {

    private final FilmeRepository filmeRepository;

    // Injecao por CONSTRUTOR, nao por campo com @Autowired. Vantagens: o campo
    // pode ser final (imutavel), a dependencia fica explicita na assinatura, e a
    // classe pode ser instanciada em um teste unitario sem subir o Spring.
    public FilmeService(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    /**
     * Busca filmes cujo titulo CONTENHA o termo informado, opcionalmente
     * filtrando tambem por ano.
     *
     * <p>Conforme o enunciado, nao e necessario informar o titulo inteiro e a
     * busca pode retornar mais de um filme. Termo ausente ou em branco significa
     * "sem filtro de titulo", devolvendo o catalogo.
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
