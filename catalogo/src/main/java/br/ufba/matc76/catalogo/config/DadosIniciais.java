package br.ufba.matc76.catalogo.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.ufba.matc76.catalogo.model.Chamado;
import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.Status;
import br.ufba.matc76.catalogo.repository.ChamadoRepository;

/**
 * Carrega chamados fictícios na primeira execução.
 * Só insere se a tabela estiver vazia, então reiniciar a aplicação não duplica dados.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final ChamadoRepository repository;

    public DadosIniciais(ChamadoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Chamado("Impressora do 2º andar não imprime", "Ana Souza", "Financeiro", Prioridade.ALTA, Status.ABERTO),
                new Chamado("Solicitar instalação do Excel", "Carlos Lima", "Financeiro", Prioridade.BAIXA, Status.RESOLVIDO),
                new Chamado("Sistema de folha de pagamento fora do ar", "Beatriz Alves", "RH", Prioridade.ALTA, Status.EM_ANDAMENTO),
                new Chamado("Troca de mouse e teclado", "Diego Ramos", "RH", Prioridade.BAIXA, Status.RESOLVIDO),
                new Chamado("Wi-Fi instável na sala de reuniões", "Fernanda Costa", "TI", Prioridade.MEDIA, Status.ABERTO),
                new Chamado("Impressora sem toner", "Gustavo Pinto", "Administrativo", Prioridade.MEDIA, Status.EM_ANDAMENTO),
                new Chamado("Servidor de arquivos lento", "Helena Martins", "TI", Prioridade.ALTA, Status.EM_ANDAMENTO),
                new Chamado("Acesso à VPN para trabalho remoto", "Igor Nunes", "Comercial", Prioridade.MEDIA, Status.RESOLVIDO),
                new Chamado("Atualização do antivírus", "Juliana Teixeira", "TI", Prioridade.BAIXA, Status.ABERTO),
                new Chamado("Erro ao emitir nota fiscal", "Karina Moura", "Financeiro", Prioridade.ALTA, Status.ABERTO),
                new Chamado("Projetor da sala 3 não liga", "Lucas Prado", "Comercial", Prioridade.MEDIA, Status.ABERTO),
                new Chamado("Criar e-mail para novo funcionário", "Marina Duarte", "RH", Prioridade.BAIXA, Status.RESOLVIDO)));
    }
}
