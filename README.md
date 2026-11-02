# Casa das Molduras

Ecommerce de molduras, quadros e espelhos com backend Java sem framework web, persistência PostgreSQL e frontend planejado em React, TypeScript e Tailwind CSS.

## Implementação atual

- Projeto Maven com Java 27 e driver PostgreSQL JDBC 42.7.14.
- Configuração de conexão por variáveis de ambiente.
- Modelo e repositório de categorias ativas.
- Verificação manual de consulta ao banco por DatabaseConnectionTest.
- Servidor HTTP local com GET /health e resposta textual OK.
- GET /api/categories com categorias ativas em JSON e tratamento de erros HTTP.
- GET /api/products com paginação, categoria, preço inicial e disponibilidade.

O endpoint de saúde verifica a resposta do servidor HTTP. A consulta ao PostgreSQL é executada separadamente pela classe de verificação. Filtros de produtos, detalhes do produto e frontend permanecem planejados.

## Estrutura

- backend/: projeto Java e configuração Maven.
- PRD.md: requisitos de produto e critérios de aceite.
- BANCO-DE-DADOS.md: especificação de persistência.
- DADOS-LOJA-DEMONSTRACAO.txt: catálogo e parâmetros operacionais de demonstração.

## Execução local

Requisitos: JDK 27, Maven ou Maven integrado do IntelliJ IDEA e PostgreSQL com o banco casa_das_molduras provisionado manualmente pelo pgAdmin 4.

Abrir backend/pom.xml como projeto Maven e sincronizar as dependências. Configurar as variáveis nas execuções de DatabaseConnectionTest e Application:

| Variável | Finalidade |
| --- | --- |
| DB_URL | URL JDBC, por exemplo jdbc:postgresql://localhost:5432/casa_das_molduras |
| DB_USER | Papel de aplicação ecommerce_app |
| DB_PASSWORD | Senha definida localmente para o papel de aplicação |

Executar br.com.casadasmolduras.DatabaseConnectionTest para listar as categorias ativas. Executar br.com.casadasmolduras.Application para iniciar o servidor em http://127.0.0.1:8080. GET /health retorna OK. GET /api/categories consulta o PostgreSQL e exige as variáveis de conexão na execução de Application. /health permanece independente do banco.

Credenciais são externas ao código. Configurações locais de execução da IDE não integram o repositório. Estrutura e dados do PostgreSQL não são criados automaticamente pela aplicação; o commit do código não constitui backup do banco.

## API de categorias

GET /api/categories retorna HTTP 200 e uma lista JSON com id, name e slug das categorias ativas, ordenadas por id. Catálogo sem categorias ativas retorna []. Acesso público, sem autenticação. A rota ainda não possui filtros ou paginação.

Métodos diferentes de GET recebem HTTP 405 com Allow: GET. Sufixos de caminho como /api/categories/123 recebem HTTP 404. Falha de banco ou configuração retorna HTTP 500 com mensagem genérica; detalhes ficam no log do servidor. Respostas da rota usam application/json; charset=UTF-8.

Serialização JSON: Gson 2.14.0. Após alterar pom.xml, recarregar o projeto Maven no IntelliJ e reiniciar Application. A configuração de execução precisa conter DB_URL, DB_USER e DB_PASSWORD; variáveis de outra configuração não são herdadas automaticamente.

## API de produtos

GET /api/products retorna HTTP 200 com items, page, pageSize e hasNext. Acesso público. Página inicial: 1; tamanho fixo: 12 produtos. GET /api/products?page=2 acessa a página seguinte. Página posterior ao catálogo retorna items: [] e hasNext: false. Não há contagem total nesta versão.

Cada item contém id, name, slug, category (id, name e slug), startingPrice, available e featured. startingPrice é o menor preço entre todas as variações ativas, inclusive esgotadas; available indica se existe variação ativa com estoque físico superior ao reservado. O preço é serializado a partir de BigDecimal. Disponibilidade da listagem é informativa e deve ser revalidada no checkout.

Produtos, categorias e variações inativos não participam da consulta. Produtos sem variações ativas não são listados; produtos com variações ativas esgotadas permanecem visíveis com available: false. Agrupamento por produto impede duplicação por variação; ordenação por ID torna a paginação determinística em um catálogo estável. A consulta busca até 13 registros para identificar a próxima página, retornando no máximo 12.

Parâmetro permitido: page, inteiro de 1 a 2147483647. Parâmetros desconhecidos, repetidos, vazios ou inválidos retornam HTTP 400 e error: INVALID_QUERY. POST e outros métodos retornam 405 com Allow: GET. Caminhos como /api/products/123 retornam 404; rota de detalhes ainda não implementada. Erros de banco/configuração retornam 500 com mensagem genérica.

Reiniciar Application após compilar; DB_URL, DB_USER e DB_PASSWORD devem estar configuradas nessa execução. O papel ecommerce_app utiliza as permissões SELECT já concedidas às tabelas de catálogo.
