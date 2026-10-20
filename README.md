# Casa das Molduras

Ecommerce de molduras, quadros e espelhos com backend Java sem framework web, persistência PostgreSQL e frontend planejado em React, TypeScript e Tailwind CSS.

## Implementação atual

- Projeto Maven com Java 27 e driver PostgreSQL JDBC 42.7.14.
- Configuração de conexão por variáveis de ambiente.
- Modelo e repositório de categorias ativas.
- Verificação manual de consulta ao banco por DatabaseConnectionTest.
- Servidor HTTP local com GET /health e resposta textual OK.

O endpoint de saúde verifica a resposta do servidor HTTP. A consulta ao PostgreSQL é executada separadamente pela classe de verificação. Endpoints de catálogo e frontend permanecem planejados.

## Estrutura

- backend/: projeto Java e configuração Maven.
- PRD.md: requisitos de produto e critérios de aceite.
- BANCO-DE-DADOS.md: especificação de persistência.
- DADOS-LOJA-DEMONSTRACAO.txt: catálogo e parâmetros operacionais de demonstração.

## Execução local

Requisitos: JDK 27, Maven ou Maven integrado do IntelliJ IDEA e PostgreSQL com o banco casa_das_molduras provisionado manualmente pelo pgAdmin 4.

Abrir backend/pom.xml como projeto Maven e sincronizar as dependências. Configurar as variáveis na execução de DatabaseConnectionTest:

| Variável | Finalidade |
| --- | --- |
| DB_URL | URL JDBC, por exemplo jdbc:postgresql://localhost:5432/casa_das_molduras |
| DB_USER | Papel de aplicação ecommerce_app |
| DB_PASSWORD | Senha definida localmente para o papel de aplicação |

Executar br.com.casadasmolduras.DatabaseConnectionTest para listar as categorias ativas. Executar br.com.casadasmolduras.Application para iniciar o servidor em http://127.0.0.1:8080. GET /health retorna OK. A configuração atual do servidor HTTP não exige conexão com o banco.

Credenciais são externas ao código. Configurações locais de execução da IDE não integram o repositório. Estrutura e dados do PostgreSQL não são criados automaticamente pela aplicação; o commit do código não constitui backup do banco.
