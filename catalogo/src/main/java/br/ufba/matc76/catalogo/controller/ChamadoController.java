package br.ufba.matc76.catalogo.controller;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.ufba.matc76.catalogo.dto.ChamadoEntradaDTO;
import br.ufba.matc76.catalogo.dto.ChamadoSaidaDTO;
import br.ufba.matc76.catalogo.model.Chamado;
import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.Status;
import br.ufba.matc76.catalogo.repository.ChamadoRepository;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoRepository repository;

    public ChamadoController(ChamadoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ChamadoSaidaDTO> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(ChamadoSaidaDTO::de).toList();
    }

    @GetMapping("/{id}")
    public ChamadoSaidaDTO porId(@PathVariable("id") Long id) {
        return ChamadoSaidaDTO.de(obter(id));
    }

    @GetMapping("/setor")
    public List<ChamadoSaidaDTO> porSetor(@RequestParam("nome") String nome) {
        return repository.findBySetorIgnoreCaseOrderByIdAsc(nome.trim())
                .stream().map(ChamadoSaidaDTO::de).toList();
    }

    @GetMapping("/prioridade")
    public List<ChamadoSaidaDTO> porPrioridade(@RequestParam("nivel") String nivel) {
        Prioridade prioridade;
        try {
            prioridade = Prioridade.valueOf(nivel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return List.of(); // nível inexistente: nenhuma correspondência
        }
        return repository.findByPrioridadeOrderByIdAsc(prioridade)
                .stream().map(ChamadoSaidaDTO::de).toList();
    }

    @GetMapping("/busca")
    public List<ChamadoSaidaDTO> busca(@RequestParam("texto") String texto) {
        return repository.findByTituloContainingIgnoreCaseOrderByIdAsc(texto.trim())
                .stream().map(ChamadoSaidaDTO::de).toList();
    }

    @GetMapping("/criticos")
    public List<ChamadoSaidaDTO> criticos() {
        return repository.findByPrioridadeAndStatusNotOrderByIdAsc(Prioridade.ALTA, Status.RESOLVIDO)
                .stream().map(ChamadoSaidaDTO::de).toList();
    }

    @GetMapping("/resumo")
    public Map<String, Long> resumo() {
        Map<String, Long> resumo = new LinkedHashMap<>();
        resumo.put("abertos", repository.countByStatus(Status.ABERTO));
        resumo.put("emAndamento", repository.countByStatus(Status.EM_ANDAMENTO));
        resumo.put("resolvidos", repository.countByStatus(Status.RESOLVIDO));
        resumo.put("total", repository.count());
        return resumo;
    }

    @PostMapping
    public ResponseEntity<ChamadoSaidaDTO> cadastrar(@RequestBody ChamadoEntradaDTO dto) {
        validar(dto);
        Status status = dto.status() != null ? dto.status() : Status.ABERTO;
        Chamado chamado = new Chamado(dto.titulo(), dto.solicitante(), dto.setor(),
                dto.prioridade(), status);
        Chamado salvo = repository.save(chamado);
        return ResponseEntity.created(URI.create("/chamados/" + salvo.getId()))
                .body(ChamadoSaidaDTO.de(salvo));
    }

    @PutMapping("/{id}")
    public ChamadoSaidaDTO atualizar(@PathVariable("id") Long id, @RequestBody ChamadoEntradaDTO dto) {
        Chamado chamado = obter(id);
        validar(dto);

        chamado.setTitulo(dto.titulo());
        chamado.setSolicitante(dto.solicitante());
        chamado.setSetor(dto.setor());
        chamado.setPrioridade(dto.prioridade());
        if (dto.status() != null) {
            chamado.setStatus(dto.status());
        }
        return ChamadoSaidaDTO.de(repository.save(chamado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        repository.delete(obter(id));
        return ResponseEntity.noContent().build();
    }

    private Chamado obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Chamado " + id + " não encontrado"));
    }

    private void validar(ChamadoEntradaDTO dto) {
        if (vazio(dto.titulo()) || vazio(dto.solicitante())
                || vazio(dto.setor()) || dto.prioridade() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe titulo, solicitante, setor e prioridade");
        }
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
