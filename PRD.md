# PRD — Casa das Molduras

Versão: 1.1  
Revisão: 19/10/2026  
Status: especificação de produto; implementação incremental  
Ambiente de referência: demonstração

## 1. Visão geral

Ecommerce de molduras, quadros e espelhos com catálogo de produtos prontos, compra online, encomendas personalizadas e administração operacional. O ambiente de demonstração utiliza catálogo ativo, estoque inicial e parâmetros comerciais definidos, permitindo executar jornadas completas sem dependência de integrações externas.

O projeto contempla frontend React com TypeScript e Tailwind CSS, API Java sem framework web e persistência PostgreSQL. O provisionamento do banco é realizado manualmente pelo pgAdmin 4, incluindo execução de SQL no Query Tool. A aplicação não cria a estrutura do banco durante a inicialização.

## 2. Objetivos

- Disponibilizar descoberta de produtos por categoria, nome, tamanho, acabamento e preço.
- Processar pedidos com validação de preços, controle de reservas e integridade transacional.
- Gerenciar propostas personalizadas, aprovação, sinal, produção e saldo.
- Centralizar catálogo, estoque, atendimento, pedidos e operações administrativas.
- Isolar integrações de pagamento, notificações e logística por adaptadores substituíveis.

Critério de conclusão: jornadas e requisitos de aceite da seção 12 executados com persistência entre reinicializações e evidências de validação. Metas comerciais de faturamento e conversão não integram os critérios de aceite técnico.

## 3. Referências e decisões de arquitetura

| Área | Definição |
| --- | --- |
| Frontend | React, TypeScript e Tailwind CSS |
| Backend | Java sem Spring Boot e sem framework web |
| Persistência | PostgreSQL via JDBC |
| Administração do banco | pgAdmin 4; provisionamento manual |
| Convenções | Tabelas em inglês no plural; campos em snake_case |
| Localização | pt-BR, BRL e America/Sao_Paulo |
| Autenticação | Conta obrigatória para compra e orçamento; sessão no servidor |
| Pagamento | Pix e cartão à vista simulados; sem movimentação financeira externa |
| Recebimento | Retirada gratuita ou entrega local por zona |
| Dados operacionais | DADOS-LOJA-DEMONSTRACAO.txt |
| Dicionário de dados | BANCO-DE-DADOS.md |

Bibliotecas auxiliares para serialização JSON, driver de banco e hashing de senha são permitidas. Servidor HTTP do JDK, Maven e Vite são opções de implementação a validar na preparação técnica; versões de dependências ainda não estão fixadas.

## 4. Perfis e autorização

**Visitante:** navegação, busca, consulta de produtos e montagem de carrinho.

**Cliente:** gestão da própria sessão, compra, consulta dos próprios pedidos e orçamentos, aprovação de propostas e abertura de solicitações de atendimento.

**Administrador:** gestão de catálogo e estoque, registro de vendas de balcão, resposta a orçamentos, acompanhamento de produção, atualização de pedidos e tratamento de pós-venda.

Autorização é verificada no backend em cada operação. Cadastro público não atribui perfil administrativo. Acesso a pedidos, orçamentos e solicitações de terceiros é proibido para clientes.

## 5. Escopo funcional

| ID | Requisito |
| --- | --- |
| RF01 | Página inicial com marca, banner estático, categorias e produtos destacados |
| RF02 | Catálogo paginado com busca, filtros combinados e ordenação por preço |
| RF03 | Detalhes com descrição, material, imagens, dimensões e variações |
| RF04 | Cadastro, login, logout e área do cliente |
| RF05 | Carrinho persistido localmente, com atualização de quantidades e remoção |
| RF06 | Checkout com revalidação de preço, disponibilidade, retirada ou entrega |
| RF07 | Pagamento simulado aprovado ou recusado e registro de tentativas |
| RF08 | Histórico e acompanhamento de pedidos |
| RF09 | Solicitação e resposta administrativa de orçamento |
| RF10 | Aprovação de proposta e criação de encomenda com sinal e saldo |
| RF11 | Administração de categorias, produtos, variações e imagens |
| RF12 | Ajuste de estoque com motivo e histórico, incluindo vendas de balcão |
| RF13 | Controle de preparação, produção, retirada e entrega |
| RF14 | Cancelamento, atendimento de avaria/troca e reembolso simulado |
| RF15 | Conteúdo institucional, contatos configuráveis e FAQ |

Fora do escopo inicial: cobranças externas, integração fiscal/ERP, transportadoras, envio nacional, consulta de CEP, configurador visual, upload de arte, cupons, avaliações, favoritos, marketplace e compra sem conta. Recuperação de senha por email e notificações externas pertencem à evolução de integrações.

As funcionalidades são entregues em incrementos; inclusão no escopo não representa implementação concluída.

## 6. Jornadas

### 6.1 Produto pronto

Catálogo → seleção de variação → carrinho → autenticação → seleção de recebimento → revisão → criação do pedido e reserva → pagamento simulado → preparação → retirada ou entrega → conclusão.

### 6.2 Encomenda

Solicitação → análise administrativa → proposta com validade → aprovação de especificações → criação de encomenda → sinal simulado → produção → saldo simulado → retirada ou entrega → conclusão.

### 6.3 Pós-venda

Solicitação vinculada ao pedido → análise administrativa → decisão registrada → refação, substituição ou reembolso simulado. A reposição de estoque depende da situação física do item.

## 7. Regras de negócio

### 7.1 Catálogo e preços

- RN01: categorias iniciais são Molduras, Quadros e Espelhos. Personalização é um serviço acessado por orçamento.
- RN02: produto comprável exige categoria, produto e variação ativos; SKU único; preço positivo; estoque disponível suficiente.
- RN03: dimensões em centímetros com referência explícita: INTERNAL para imagem/espelho e EXTERNAL para dimensão total.
- RN04: produtos esgotados podem permanecer visíveis, com compra indisponível. Registros vinculados a pedidos são desativados, preservando histórico.
- RN05: valores em BRL, com representação decimal exata e duas casas decimais. Cada componente de orçamento é arredondado antes da soma.
- RN06: subtotal é a soma de preço unitário multiplicado pela quantidade. Total é subtotal mais taxa de entrega; retirada gratuita; descontos não previstos.
- RN07: preço e disponibilidade são revalidados no backend. Divergências exigem nova revisão antes da criação do pedido.
- RN08: itens do pedido preservam nome, SKU, medidas, acabamento, preço e quantidade da compra. Taxa, endereço e modalidade de recebimento também são registrados como dados da operação.

### 7.2 Carrinho, estoque e transações

- RN09: quantidades são inteiras positivas; itens da mesma variação são agrupados. Carrinho não reserva estoque.
- RN10: estoque disponível = stock_quantity - reserved_quantity. Quantidades não podem ser negativas; reservas não excedem estoque físico.
- RN11: criação de pedido e reserva de todos os itens ocorrem em uma transação. Falha em qualquer item impede a criação parcial.
- RN12: checkout possui chave de idempotência vinculada ao cliente e ao conteúdo confirmado. Repetição retorna a operação existente; reutilização com conteúdo divergente é rejeitada.
- RN13: reserva de produto pronto expira em 30 minutos. Pagamento recusado permite nova tentativa sem renovar esse prazo.
- RN14: aprovação dentro do prazo confirma o pedido, baixa estoque físico e remove reserva atomicamente. Repetição da aprovação não produz nova baixa.
- RN15: cancelamento não pago ou expiração libera reserva uma única vez. Expiração é verificada ao pagar, periodicamente e após reinicialização.
- RN16: pagamento, cancelamento e expiração concorrentes resultam em um único estado consistente.
- RN17: ajustes e vendas de balcão respeitam reservas e registram motivo, origem, responsável e data. Estoque disponível inferior a três unidades gera alerta administrativo.
- RN18: cargas iniciais não reinicializam estoque ou reservas em execução. Alterações posteriores são movimentações rastreáveis.

### 7.3 Pagamento, entrega e pós-venda

- RN19: produtos prontos exigem pagamento integral. Encomendas exigem sinal de 50% e saldo antes da liberação. O saldo corresponde ao total menos o sinal arredondado.
- RN20: cobrança e reembolso são simulados. A interface identifica o ambiente; não coleta dados reais de cartão nem gera cobrança externa.
- RN21: zonas CENTRAL e NEIGHBORHOODS custam R$ 15,00 e R$ 25,00; prazos de entrega de um e dois dias úteis após preparação. OUTSIDE permite apenas retirada.
- RN22: peças prontas exigem um dia útil de preparação; encomendas de moldura, cinco; de espelho, dez. Dias úteis são segunda a sexta, sem calendário de feriados no escopo inicial.
- RN23: retirada ocorre no horário de atendimento, mediante identificação do pedido e do cliente. Entrega exige endereço e zona.
- RN24: cliente cancela diretamente apenas antes do pagamento. Cancelamento pago antes da preparação é administrativo, com reembolso simulado e reposição de estoque única.
- RN25: avaria, alteração após início de produção e devolução exigem análise registrada. Produtos avariados não retornam automaticamente ao estoque vendável. Solicitação de troca sem avaria possui janela de 30 dias no cenário de validação.

### 7.4 Orçamentos e encomendas

- RN26: solicitação exige tipo de peça, largura, altura, acabamento e observações. Medidas devem ser positivas. Intervalo padrão: 10 a 100 cm por lado; valores externos ao intervalo exigem análise manual.
- RN27: proposta discrimina componentes, preço, prazo e validade de sete dias corridos. A tabela de custos e fórmulas está definida na especificação operacional.
- RN28: orçamento isolado não reserva estoque nem contabiliza venda. Aprovação válida gera no máximo uma encomenda, preservando a versão da proposta e suas condições.
- RN29: produção inicia após aprovação das especificações e confirmação do sinal, considerando o último evento. Encomendas não baixam variações prontas; insumos são conferidos administrativamente.
- RN30: alterações de especificação antes da produção geram revisão de proposta e nova aprovação. Aprovação vencida ou de versão desatualizada é rejeitada.
- RN31: entregas de encomendas incluem a taxa escolhida no total confirmado antes do cálculo do sinal. Propostas exemplificadas com retirada possuem taxa zero.

## 8. Ciclos operacionais

Os rótulos abaixo descrevem estados de negócio; os identificadores persistidos serão definidos no contrato técnico de pedidos.

| Fluxo | Transições |
| --- | --- |
| Produto pronto | Aguardando pagamento → Confirmado → Em preparação |
| Reserva encerrada | Aguardando pagamento → Cancelado ou Expirado |
| Cancelamento pago elegível | Confirmado → Cancelado, com registro de reembolso simulado |
| Retirada | Em preparação → Pronto para retirada → Concluído |
| Entrega | Em preparação → Pronto para entrega → Em entrega → Concluído |
| Orçamento | Recebido → Em análise → Respondido → Aprovado ou Expirado |
| Cancelamento de solicitação | Recebido ou Em análise → Cancelado |
| Encomenda | Aguardando sinal → Em produção → Aguardando saldo → Pronto para retirada ou Pronto para entrega |

Encomendas somente entram em produção com especificações aprovadas e sinal confirmado. Pagamento recusado é uma tentativa registrada, sem avanço de estado. Orçamentos aprovados não geram múltiplas encomendas. Histórico registra transição, data e responsável. Transições não previstas são rejeitadas.

## 9. Experiência e identidade visual

Páginas: início, catálogo, produto, personalização, carrinho, autenticação, checkout, pedidos, orçamentos, atendimento, informações institucionais e painel administrativo.

Página inicial: faixa institucional, cabeçalho com busca/conta/carrinho, navegação por categorias, banner estático, cartões de categoria, vitrine de destaques, chamada para personalização e rodapé. Conteúdo institucional é carregado de configuração. Campos de contato externo não configurados não geram links ativos.

| Uso | Cor |
| --- | --- |
| Navegação e texto principal | #171717 |
| Superfícies escuras | #2B2D2F |
| Fundo | #F5EFE4 |
| Cartões | #FFFCF7 |
| Destaques | #B58A45 |
| Texto de destaque em fundo claro | #765521 |
| Divisórias | #DED2BF |

Títulos serifados e texto funcional sem serifa. Botões dourados com texto escuro; contraste validado na implementação. Imagens com proporção consistente e texto alternativo. Grade adaptável, até quatro colunas no desktop. Seleção de variação obrigatória antes da compra. Estados de carregamento, vazio, erro e sucesso em todos os fluxos assíncronos.

## 10. Requisitos não funcionais

- RNF01: fluxos utilizáveis em larguras de 360, 768 e 1440 px, sem rolagem horizontal da página.
- RNF02: navegação por teclado, foco visível, rótulos de formulário e erros associados aos campos; estado não comunicado somente por cor.
- RNF03: senhas protegidas por biblioteca de hashing apropriada e salt; consultas parametrizadas; validação no servidor; segredos fora do repositório.
- RNF04: sessão no servidor, cookie HttpOnly, Secure em produção e proteção CSRF. Logout invalida a sessão.
- RNF05: logs não incluem senhas, tokens de sessão ou dados pessoais desnecessários. Ambiente de demonstração utiliza identidades sintéticas.
- RNF06: atomicidade e controle de concorrência para reservas, pagamentos e cancelamentos; operações financeiras e de estoque idempotentes.
- RNF07: catálogo paginado em 12 itens, imagens dimensionadas e carregamento adiado fora da primeira tela.
- RNF08: meta de latência de catálogo de até um segundo em 95% de 100 requisições locais sequenciais, com 500 produtos; ambiente de medição documentado.
- RNF09: separação entre transporte HTTP, regras de negócio e persistência; configuração externa ao código de interface.
- RNF10: instruções de provisionamento e backup/restauração documentadas; credencial da aplicação com permissões limitadas.

## 11. Arquitetura e modelo de domínio

Fluxo: React/TypeScript → API HTTP Java → JDBC → PostgreSQL.

Handlers tratam entrada/saída HTTP; services aplicam regras e delimitam transações; repositories executam SQL; DTOs definem contratos. Integrações usam interfaces com adaptadores simulados. O frontend não é autoridade para preço, estoque ou autorização.

Entidades previstas: categories, products, product_variants, product_images, users, sessions, orders, order_items, payment_attempts, stock_movements, order_history, quotes, quote_revisions, custom_orders, refunds e support_requests. A modelagem física além do catálogo permanece planejada.

Dados históricos de pedido não dependem de valores mutáveis do catálogo. Carrinho local não exige tabela no escopo inicial. Encomendas e produtos prontos possuem regras distintas de estoque e pagamento; essa distinção deve constar no modelo de pedidos.

## 12. Critérios de aceite

1. Busca, filtros combinados e paginação retornam produtos compatíveis e apresentam estado vazio quando necessário.
2. Autenticação preserva o carrinho; checkout calcula corretamente retirada e ambas as zonas de entrega.
3. Valores adulterados no navegador são rejeitados; alterações de preço exigem nova revisão.
4. Compras simultâneas da última unidade produzem no máximo uma reserva válida.
5. Repetição de checkout, aprovação de pagamento ou liberação de reserva não duplica efeitos.
6. Pagamento recusado mantém o prazo original; pedido expirado não aceita aprovação posterior.
7. Reinicialização preserva pedidos e permite recuperar reservas vencidas.
8. Cliente não acessa recursos de outro cliente nem endpoints administrativos.
9. Desativação de catálogo preserva itens e valores históricos dos pedidos.
10. Ajustes não reduzem estoque físico abaixo do reservado; operações de balcão respeitam a mesma disponibilidade.
11. Orçamento vencido não pode ser aprovado; aprovação válida cria uma única encomenda com sinal e saldo corretos.
12. Produção exige aprovação e sinal; liberação exige saldo. Encomendas não consomem estoque de produtos prontos.
13. Cancelamento pago elegível registra reembolso e reposição uma vez; avaria não repõe estoque automaticamente.
14. Transições inválidas são rejeitadas e transições válidas são rastreáveis.
15. Fluxos essenciais funcionam por teclado e nas três larguras de referência.
16. Dados iniciais permitem executar CT-01 e CT-02 da especificação operacional; a execução dos testes deve ser registrada separadamente.

## 13. Plano de entrega

| Incremento | Entregável | Validação |
| --- | --- | --- |
| Fundação | Estrutura de projetos, conexão JDBC e catálogo | API acessa dados persistidos |
| Vitrine | Home, catálogo, filtros e detalhes | Navegação e seleção de variação |
| Identidade e gestão | Autenticação, autorização e administração | Isolamento de acesso e gestão do catálogo |
| Compra | Carrinho, reservas, checkout e pagamento simulado | Concorrência, idempotência e expiração |
| Operação | Preparação, zonas de entrega e vendas de balcão | Integridade de estoque e transições |
| Personalização | Orçamento, aprovação, sinal, produção e saldo | Jornada completa de encomenda |
| Pós-venda | Atendimento, cancelamento e reembolso simulado | Auditoria e reposição consistente |
| Qualidade | Acessibilidade, desempenho e documentação | Critérios de aceite verificados |

## 14. Dependências e limites

Assets de imagem e versão responsiva do logo permanecem pendentes. Catálogo e regras comerciais de demonstração estão especificados e não dependem de levantamento externo. Contratos de API, esquema transacional de pedidos e configuração de runtime serão detalhados nos respectivos incrementos.

Integrações financeiras, fiscais e logísticas externas, políticas de operação comercial e configuração de produção exigem validação específica antes de implantação fora do ambiente de demonstração. O status de implementação deve ser comprovado por código, execução e testes; este documento define requisitos.
