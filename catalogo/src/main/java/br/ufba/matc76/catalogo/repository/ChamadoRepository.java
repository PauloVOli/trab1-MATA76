package br.ufba.matc76.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufba.matc76.catalogo.model.Chamado;
import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.Status;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findAllByOrderByIdAsc();

    List<Chamado> findBySetorIgnoreCaseOrderByIdAsc(String setor);

    List<Chamado> findByPrioridadeOrderByIdAsc(Prioridade prioridade);

    List<Chamado> findByTituloContainingIgnoreCaseOrderByIdAsc(String texto);

    List<Chamado> findByPrioridadeAndStatusNotOrderByIdAsc(Prioridade prioridade, Status status);

    long countByStatus(Status status);
}
