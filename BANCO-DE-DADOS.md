# Banco de dados — especificação técnica

Versão: 1.1  
Revisão: 19/10/2026  
SGBD: PostgreSQL  
Banco: casa_das_molduras  
Schema: public

## 1. Administração e convenções

Provisionamento manual pelo pgAdmin 4, por interface ou SQL no Query Tool. A aplicação Java utiliza JDBC para acesso ao banco existente e não executa criação automática de schema na inicialização.

Tabelas em inglês, no plural, e campos em snake_case. Identificadores de entidade: bigint gerado como identity e chave primária. Valores monetários: numeric com duas casas decimais. Exclusão de registros referenciados é restrita; produtos e categorias suportam desativação lógica.

Esta especificação descreve o contrato de dados do catálogo. Não constitui uma inspeção em tempo real da instância PostgreSQL nem comprova a implantação das estruturas planejadas.

## 2. Dicionário de dados

### categories

| Campo | Tipo | Nulo | Restrição/padrão |
| --- | --- | --- | --- |
| id | bigint | Não | GENERATED ALWAYS AS IDENTITY; PK |
| name | varchar(100) | Não | Nome da categoria |
| slug | varchar(120) | Não | UNIQUE |
| is_active | boolean | Não | DEFAULT true |

### products

| Campo | Tipo | Nulo | Restrição/padrão |
| --- | --- | --- | --- |
| id | bigint | Não | GENERATED ALWAYS AS IDENTITY; PK |
| category_id | bigint | Não | FK categories.id; ON DELETE RESTRICT |
| name | varchar(160) | Não | Nome comercial |
| slug | varchar(180) | Não | UNIQUE |
| description | text | Não | Descrição comercial |
| material | varchar(120) | Não | Composição principal |
| is_featured | boolean | Não | DEFAULT false |
| is_active | boolean | Não | DEFAULT false |

O padrão is_active=false permite cadastro em rascunho. O conjunto de demonstração exige ativação explícita dos produtos após provisionamento das variações válidas.

### product_variants

| Campo | Tipo | Nulo | Restrição/padrão |
| --- | --- | --- | --- |
| id | bigint | Não | GENERATED ALWAYS AS IDENTITY; PK |
| product_id | bigint | Não | FK products.id; ON DELETE RESTRICT |
| sku | varchar(80) | Não | UNIQUE |
| width_cm | numeric(8,2) | Não | CHECK > 0 |
| height_cm | numeric(8,2) | Não | CHECK > 0 |
| measurement_reference | varchar(20) | Não | CHECK IN ('INTERNAL', 'EXTERNAL') |
| finish | varchar(120) | Não | Acabamento/cor |
| price | numeric(12,2) | Não | CHECK > 0 |
| stock_quantity | integer | Não | DEFAULT 0; CHECK >= 0 |
| reserved_quantity | integer | Não | DEFAULT 0; CHECK >= 0 e <= stock_quantity |
| is_active | boolean | Não | DEFAULT true |

Disponibilidade = stock_quantity - reserved_quantity. O resultado é calculado, sem coluna persistida adicional. Preço e estoque pertencem à variação, permitindo configurações comerciais diferentes para um mesmo produto.

### product_images — estrutura planejada

| Campo | Tipo | Nulo | Restrição/padrão |
| --- | --- | --- | --- |
| id | bigint | Não | GENERATED ALWAYS AS IDENTITY; PK |
| product_id | bigint | Não | FK products.id; ON DELETE RESTRICT |
| image_url | text | Não | URL ou caminho público de recurso existente |
| alt_text | varchar(250) | Não | Descrição acessível |
| display_order | integer | Não | CHECK >= 0; UNIQUE com product_id |

Arquivos binários não integram esta tabela. image_asset_planned, presente na especificação operacional, é uma referência de produção de mídia e não um campo do schema.

## 3. Relacionamentos e índices

- categories 1:N products.
- products 1:N product_variants.
- products 1:N product_images.
- Índice planejado em products.category_id.
- Índice planejado em product_variants.product_id.
- Restrição UNIQUE(product_id, display_order) em product_images também atende consultas por produto.

Ordem de provisionamento: categories → products → product_variants → product_images. As chaves naturais slug e sku identificam registros nas cargas de dados; IDs não devem ser presumidos como constantes entre ambientes.

## 4. Integridade e responsabilidades

O banco valida chaves, unicidade, obrigatoriedade e limites numéricos. O backend valida publicação, autorização, existência de variação comprável e situação da categoria.

Reserva, baixa e liberação de estoque exigem transações com controle de concorrência. O estoque físico não pode ser reduzido abaixo das reservas. Movimentações posteriores ao provisionamento devem registrar origem, quantidade, motivo, responsável e data.

Mudanças no catálogo não alteram snapshots de itens de pedidos. Atualizações do mesmo item preservam identificadores; produtos distintos geram novos registros.

## 5. Conjunto inicial de dados

Fonte: DADOS-LOJA-DEMONSTRACAO.txt.

Três categorias e oito produtos com uma variação inicial por produto. Quantidades de reserva iniciais iguais a zero e estoque físico conforme especificação. Produtos e variações ativos; destaques definidos pelo campo is_featured.

As cargas não são executadas automaticamente. Reexecução deve evitar duplicações por slug/SKU sem sobrescrever estoque consumido, reservas ou histórico. Datas, preços, estados e configurações de pedidos existentes não são recalculados a partir de novos valores do catálogo.

## 6. Validação do catálogo

| Caso | Resultado esperado |
| --- | --- |
| Inserção de categoria e produto válido | Registros persistidos e relacionamento válido |
| Slug ou SKU repetido | Rejeição por unicidade |
| category_id ou product_id inexistente | Rejeição por chave estrangeira |
| Preço ou dimensão não positiva | Rejeição por CHECK |
| Reserva negativa ou superior ao estoque físico | Rejeição por CHECK |
| Exclusão de categoria com produto associado | Rejeição por restrição referencial |
| Produto com duas variações | Preços e quantidades independentes |
| Reinicialização do cliente de administração | Dados preservados no servidor |

Validações transacionais de concorrência dependem da implementação dos serviços de pedidos, além das restrições do catálogo.

## 7. Modelagem planejada

- Identidade: users e sessions; email único, hash de senha, perfil e expiração/revogação de sessão.
- Compra: orders e order_items; cliente, modalidade, endereço, zona, taxa, total, idempotência, prazo de reserva e snapshots dos itens.
- Pagamento: payment_attempts e refunds; identificação da operação, resultado e prevenção de processamento duplicado.
- Auditoria: stock_movements e order_history.
- Personalização: quotes, quote_revisions e custom_orders; especificações, validade, aprovação, custos, sinal, saldo e produção.
- Atendimento: support_requests; vínculo com pedido, motivo, análise e resolução.

A modelagem física dessas entidades permanece pendente. Encomendas não consomem variações prontas. Taxa de entrega integra o total confirmado antes do cálculo do sinal. Histórico financeiro e operacional deve ser preservado.

## 8. Configuração e operação

Conexão local configurada por host, porta, nome do banco e credenciais externas ao código-fonte. A aplicação utiliza papel com permissões limitadas; provisionamento utiliza credencial administrativa separada.

Backups são administrados pelo pgAdmin e devem possuir procedimento de restauração validado antes da operação com dados persistentes relevantes. Senhas e tokens não integram a documentação ou o conjunto público de demonstração.
