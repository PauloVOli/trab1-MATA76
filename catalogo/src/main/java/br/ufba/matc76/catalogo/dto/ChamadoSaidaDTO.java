package br.ufba.matc76.catalogo.dto;

import br.ufba.matc76.catalogo.model.Chamado;
import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.Status;

public record ChamadoSaidaDTO(Long id, String titulo, String solicitante, String setor,
                              Prioridade prioridade, Status status) {

    public static ChamadoSaidaDTO de(Chamado c) {
        return new ChamadoSaidaDTO(c.getId(), c.getTitulo(), c.getSolicitante(),
                c.getSetor(), c.getPrioridade(), c.getStatus());
    }
}
