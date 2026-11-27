package br.ufba.matc76.catalogo.controller;


import br.ufba.matc76.catalogo.model.Chamado;
import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.Status;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final List<Chamado> chamados = new CopyOnWriteArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public ChamadoController() {
        adicionar("Impressora do 2º andar não imprime", "Ana Souza", "Financeiro", Prioridade.ALTA, Status.ABERTO);
        adicionar("Solicitar instalação do Excel", "Carlos Lima", "Financeiro", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar("Sistema de folha de pagamento fora do ar", "Beatriz Alves", "RH", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar("Troca de mouse e teclado", "Diego Ramos", "RH", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar("Wi-Fi instável na sala de reuniões", "Fernanda Costa", "TI", Prioridade.MEDIA, Status.ABERTO);
        adicionar("Impressora sem toner", "Gustavo Pinto", "Administrativo", Prioridade.MEDIA, Status.EM_ANDAMENTO);
        adicionar("Servidor de arquivos lento", "Helena Martins", "TI", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar("Acesso à VPN para trabalho remoto", "Igor Nunes", "Comercial", Prioridade.MEDIA, Status.RESOLVIDO);
        adicionar("Atualização do antivírus", "Juliana Teixeira", "TI", Prioridade.BAIXA, Status.ABERTO);
        adicionar("Erro ao emitir nota fiscal", "Karina Moura", "Financeiro", Prioridade.ALTA, Status.ABERTO);
        adicionar("Projetor da sala 3 não liga", "Lucas Prado", "Comercial", Prioridade.MEDIA, Status.ABERTO);
        adicionar("Criar e-mail para novo funcionário", "Marina Duarte", "RH", Prioridade.BAIXA, Status.RESOLVIDO);
    }

    private Chamado adicionar(String titulo, String solicitante, String setor,
                              Prioridade prioridade, Status status) {
        Chamado c = new Chamado(proximoId.getAndIncrement(), titulo, solicitante, setor, prioridade, status);
        chamados.add(c);
        return c;
    }

    @GetMapping
    public List<Chamado> listar() {
        return chamados;
    }

    @GetMapping("/{id}")
    public Chamado buscarPorId(@PathVariable("id") Long id) {
        return chamados.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Chamado " + id + " não encontrado"));
    }

    @GetMapping("/setor")
    public List<Chamado> porSetor(@RequestParam("nome") String nome) {
        return chamados.stream()
                .filter(c -> c.setor().equalsIgnoreCase(nome.trim()))
                .toList();
    }

    @GetMapping("/prioridade")
    public List<Chamado> porPrioridade(@RequestParam("nivel") String nivel) {
        return chamados.stream()
                .filter(c -> c.prioridade().name().equalsIgnoreCase(nivel.trim()))
                .toList();
    }

    @GetMapping("/busca")
    public List<Chamado> busca(@RequestParam("texto") String texto) {
        String termo = texto.trim().toLowerCase();
        return chamados.stream()
                .filter(c -> c.titulo().toLowerCase().contains(termo))
                .toList();
    }

    @GetMapping("/criticos")
    public List<Chamado> criticos() {
        return chamados.stream()
                .filter(c -> c.prioridade() == Prioridade.ALTA && c.status() != Status.RESOLVIDO)
                .toList();
    }

    @GetMapping("/resumo")
    public Map<String, Long> resumo() {
        Map<String, Long> resumo = new LinkedHashMap<>();
        resumo.put("abertos", contar(Status.ABERTO));
        resumo.put("emAndamento", contar(Status.EM_ANDAMENTO));
        resumo.put("resolvidos", contar(Status.RESOLVIDO));
        resumo.put("total", (long) chamados.size());
        return resumo;
    }

    private long contar(Status status) {
        return chamados.stream().filter(c -> c.status() == status).count();
    }

    @PostMapping
    public ResponseEntity<Chamado> cadastrar(@RequestBody Chamado entrada) {
        if (vazio(entrada.titulo()) || vazio(entrada.solicitante())
                || vazio(entrada.setor()) || entrada.prioridade() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe titulo, solicitante, setor e prioridade");
        }
        Status status = entrada.status() != null ? entrada.status() : Status.ABERTO;
        Chamado novo = adicionar(entrada.titulo(), entrada.solicitante(),
                entrada.setor(), entrada.prioridade(), status);
        return ResponseEntity.created(URI.create("/chamados/" + novo.id())).body(novo);
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}