package br.ufba.matc76.catalogo.dto;

import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.Status;

public record ChamadoEntradaDTO(String titulo, String solicitante, String setor,
                                Prioridade prioridade, Status status) {
}
