# Sistema de Chamados — API REST com Spring Boot

API REST para gerenciar chamados de suporte. Os dados ficam **em memória** (sem banco de dados, JPA, Service, Repository ou autenticação).

## Estrutura do projeto

```
src/main/java/br/ufba/matc76/catalogo/
├── CatalogoApplication.java      (classe que inicia a aplicação)
├── controller/
│   └── ChamadoController.java    (endpoints e lista em memória com 12 chamados iniciais)
└── model/
    ├── Chamado.java              (id, titulo, solicitante, setor, prioridade, status)
    ├── Prioridade.java           (BAIXA, MEDIA, ALTA)
    └── Status.java               (ABERTO, EM_ANDAMENTO, RESOLVIDO)
```

## Como executar

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/chamados` | Lista todos os chamados |
| GET | `/chamados/{id}` | Consulta por ID (404 se não existir) |
| GET | `/chamados/setor?nome=financeiro` | Filtra por setor |
| GET | `/chamados/prioridade?nivel=alta` | Filtra por prioridade |
| GET | `/chamados/busca?texto=impressora` | Busca no título |
| GET | `/chamados/criticos` | Prioridade ALTA e status diferente de RESOLVIDO |
| GET | `/chamados/resumo` | Quantidade por status e total |
| POST | `/chamados` | Cadastra um chamado (retorna 201) |

Regras:
- Setor, prioridade e busca por título **não diferenciam maiúsculas de minúsculas**.
- No POST, o `id` é gerado pelo servidor e o `status` é opcional (padrão `ABERTO`).
- Se faltar `titulo`, `solicitante`, `setor` ou `prioridade`, o POST retorna 400.

## Testes e demonstração

> Os resultados abaixo valem para os 12 chamados iniciais.

### 1. Listar todos
```bash
curl -i http://localhost:8080/chamados
```
**Resposta:** `200 OK` com um array JSON de 12 chamados (ids 1 a 12).
**Explicação:** retorna toda a lista em memória.

### 2. Consultar por ID
```bash
curl -i http://localhost:8080/chamados/3
```
```json
{"id":3,"titulo":"Sistema de folha de pagamento fora do ar","solicitante":"Beatriz Alves","setor":"RH","prioridade":"ALTA","status":"EM_ANDAMENTO"}
```
**Explicação:** `200 OK` com o chamado de id 3.

### 3. ID inexistente
```bash
curl -i http://localhost:8080/chamados/99
```
**Resposta:** `404 Not Found`.
**Explicação:** nenhum chamado tem id 99.

### 4. Filtrar por setor (maiúsculas)
```bash
curl -i "http://localhost:8080/chamados/setor?nome=FINANCEIRO"
```
**Resposta:** `200 OK` com os chamados de ids **1, 2 e 10**.
**Explicação:** o filtro ignora maiúsculas/minúsculas.

### 5. Setor sem correspondência
```bash
curl -i "http://localhost:8080/chamados/setor?nome=juridico"
```
**Resposta:** `200 OK` com `[]`.
**Explicação:** não há chamados desse setor, então a lista vem vazia.

### 6. Filtrar por prioridade
```bash
curl -i "http://localhost:8080/chamados/prioridade?nivel=alta"
```
**Resposta:** `200 OK` com os chamados de ids **1, 3, 7 e 10**.
**Explicação:** todos têm prioridade ALTA, em qualquer status.

### 7. Buscar pelo título
```bash
curl -i "http://localhost:8080/chamados/busca?texto=IMPRESSORA"
```
**Resposta:** `200 OK` com os chamados de ids **1 e 6**.
**Explicação:** a busca ignora maiúsculas/minúsculas e procura o texto em qualquer parte do título.

### 8. Chamados críticos
```bash
curl -i http://localhost:8080/chamados/criticos
```
**Resposta:** `200 OK` com os chamados de ids **1, 3, 7 e 10**.
**Explicação:** combina prioridade ALTA com status diferente de RESOLVIDO.

### 9. Resumo por status (antes do cadastro)
```bash
curl -i http://localhost:8080/chamados/resumo
```
```json
{"abertos":5,"emAndamento":3,"resolvidos":4,"total":12}
```
**Explicação:** conta os chamados de cada status e o total.

### 10. Cadastrar novo chamado
```bash
curl -i -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Notebook não liga","solicitante":"Paulo Reis","setor":"TI","prioridade":"ALTA"}'
```
**Resposta:** `201 Created`, header `Location: /chamados/13` e corpo:
```json
{"id":13,"titulo":"Notebook não liga","solicitante":"Paulo Reis","setor":"TI","prioridade":"ALTA","status":"ABERTO"}
```
**Explicação:** o servidor gerou o id 13 e definiu o status `ABERTO`.

### 11. Verificar o cadastro
```bash
curl -i http://localhost:8080/chamados/13
curl -i http://localhost:8080/chamados/resumo
curl -i http://localhost:8080/chamados/criticos
```
**Resposta:** o chamado 13 é retornado, o resumo passa a `{"abertos":6,"emAndamento":3,"resolvidos":4,"total":13}` e o chamado 13 aparece também em `/criticos`.
**Explicação:** confirma que o novo chamado foi guardado e entra nas demais consultas.

### 12. Cadastro inválido
```bash
curl -i -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{"titulo":"","setor":"TI"}'
```
**Resposta:** `400 Bad Request`.
**Explicação:** faltam campos obrigatórios, então nada é cadastrado.

## Análise técnica

### Como o Spring Boot recebe e processa requisições HTTP
O Spring Boot já vem com um servidor web embutido (Tomcat). Quando chega uma requisição, o Spring olha o método HTTP (GET, POST...) e o caminho (`/chamados`) e procura o método do controller mapeado para eles. Ele executa esse método, passando os parâmetros da requisição, e transforma o retorno em uma resposta HTTP com status e corpo.

### Função do Controller
O Controller é a porta de entrada da aplicação. Ele define as rotas, recebe os dados da requisição, executa a lógica (filtrar, buscar, cadastrar) e escolhe o status da resposta (200, 201, 400, 404). Neste trabalho, como não há Service nem Repository, o `ChamadoController` também guarda a lista de chamados.

### Conversão entre objetos Java e JSON
Quem faz isso é a biblioteca Jackson, que o Spring Boot já configura sozinho. Na entrada (`@RequestBody`), o JSON recebido vira um objeto `Chamado`. Na saída, os objetos e listas retornados pelo controller viram JSON. Os nomes dos campos do JSON correspondem aos do `Chamado`, e textos como `"ALTA"` viram o enum `Prioridade`.

### Por que os chamados cadastrados somem ao reiniciar
Os chamados ficam em uma lista Java na memória da aplicação. Quando a aplicação é encerrada, essa memória é apagada. Ao iniciar de novo, a lista é recriada só com os 12 chamados iniciais. Como não há banco de dados nem arquivo, nada do que foi cadastrado é salvo.