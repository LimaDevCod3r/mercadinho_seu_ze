# Mercadinho Seu Zé - API de PDV

Sistema de Ponto de Venda (PDV) para mercadinho, desenvolvido com Spring Boot. Gerencia categorias, produtos, estoque via movimentações, vendas com cálculo de troco e histórico de movimentações.

## Stack Técnica

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.4 |
| MySQL | 8.x |
| JPA/Hibernate | - |
| Lombok | - |
| Bean Validation | - |
| JUnit 5 + Mockito | Testes unitários |

## Como Executar

```bash
# Configurar o banco de dados em application.properties
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

---

## Endpoints

### Categorias

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/categories` | Criar categoria | 201 |
| `GET` | `/categories` | Listar todas | 200 |
| `GET` | `/categories/{id}/products` | Produtos da categoria (paginado) | 200 |
| `PATCH` | `/categories/{id}` | Atualizar categoria | 204 |
| `DELETE` | `/categories/{id}` | Excluir categoria | 204 |

#### POST /categories

```json
{
  "name": "Bebidas"
}
```

**Response 201:**
```json
{
  "id": 1,
  "name": "Bebidas"
}
```

#### PATCH /categories/{id}

```json
{
  "name": "Bebidas Geladas"
}
```

**Cenários:**
- Categoria encontrada -> 204
- Categoria não encontrada -> 404

---

### Produtos

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/products` | Criar produto | 201 |
| `GET` | `/products` | Listar produtos (paginado) | 200 |
| `GET` | `/products/{id}` | Buscar por ID | 200 |
| `PATCH` | `/products/{id}` | Atualizar produto | 204 |
| `DELETE` | `/products/{id}` | Excluir produto | 204 |

#### POST /products

```json
{
  "name": "Coca-Cola 2L",
  "sale_price": 10.50,
  "category_id": 1
}
```

**Response 201:**
```json
{
  "id": 1,
  "name": "Coca-Cola 2L",
  "sale_price": 10.50,
  "category": "Bebidas",
  "category_id": 1
}
```

#### PATCH /products/{id}

```json
{
  "name": "Coca-Cola 2.5L",
  "sale_price": 12.00,
  "category_id": 2
}
```

#### DELETE /products/{id}

Exclui o produto e também remove o Stock e StockMovement associados (limpeza em cascata no nível de serviço).

**Cenários:**
- Produto encontrado -> 204
- Produto não encontrado -> 404
- Nome já cadastrado -> 409

**Paginação:** `GET /products?page=0&size=10&sort=name,asc`

```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5
}
```

---

### Estoque

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/stocks` | Criar estoque (uma única vez) | 201 |
| `GET` | `/stocks` | Listar estoques (paginado) | 200 |
| `GET` | `/stocks/{id}` | Buscar estoque por ID | 200 |
| `GET` | `/stocks/product/{id}` | Buscar estoque do produto | 200 |

#### POST /stocks

```json
{
  "productId": 1,
  "quantity": 100
}
```

**Atenção:** Este endpoint só deve ser chamado uma vez por produto. Para reajuste de estoque, use `POST /stock-movements`.

**Cenários:**
- Estoque criado com sucesso -> 201
- Estoque já existe -> 409
- Produto não encontrado -> 404

---

### Movimentações de Estoque

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/stock-movements` | Registrar movimentação | 201 |
| `GET` | `/stock-movements` | Listar movimentações (paginado) | 200 |

#### POST /stock-movements

Tipos de movimentação:

| Tipo | Descrição | Efeito no estoque |
|---|---|---|
| `ENTRY` | Entrada | Soma ao estoque |
| `EXIT` | Saída (venda) | Subtrai do estoque |
| `LOSS` | Perda | Subtrai do estoque |
| `ADJUSTMENT` | Ajuste | Define o valor exato |

**Exemplo - Entrada:**
```json
{
  "productId": 1,
  "quantity": 20,
  "type": "ENTRY",
  "reason": "Compra fornecedor"
}
```

**Exemplo - Perda:**
```json
{
  "productId": 1,
  "quantity": 5,
  "type": "LOSS",
  "reason": "Produto vencido"
}
```

**Exemplo - Ajuste (define o estoque para o valor informado):**
```json
{
  "productId": 1,
  "quantity": 100,
  "type": "ADJUSTMENT",
  "reason": "Inventário"
}
```

**Cenários:**
- Movimentação registrada -> 201
- Estoque insuficiente para EXIT/LOSS -> 409
- Produto não encontrado -> 404
- Estoque não encontrado -> 404

---

### Vendas

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/sales` | Criar venda | 201 |

#### POST /sales

```json
{
  "amountPaid": 50.00,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 3 }
  ]
}
```

**Fluxo:**
1. Busca cada produto e seu preço
2. Verifica estoque disponível
3. Decrementa o estoque de cada item
4. Calcula o total da venda
5. Valida se `amountPaid` é suficiente
6. Calcula o troco (`change = amountPaid - total`)
7. Salva a venda e os itens

**Response 201:**
```json
{
  "saleId": 1,
  "saleDate": "2026-04-05T14:30:00",
  "items": [
    {
      "productName": "Coca-Cola 2L",
      "quantity": 2,
      "unitPrice": 10.00,
      "totalPrice": 20.00
    }
  ],
  "total": 20.00,
  "amountPaid": 50.00,
  "change": 30.00
}
```

**Cenários:**
- Venda realizada com sucesso -> 201
- Produto não encontrado -> 500
- Estoque insuficiente -> 500
- Valor pago insuficiente -> 500
- Lista de itens vazia -> 500

---

## Formato de Erro

### Erro padrão (404, 409, 500)

```json
{
  "error": "Mensagem de erro",
  "status": 404,
  "path": "/products/99",
  "timestamp": "2026-04-05T14:30:00"
}
```

### Erro de validação (400)

```json
{
  "status": 400,
  "error": "Erro de Validação",
  "path": "/products",
  "fields": {
    "name": "não deve estar em branco",
    "sale_price": "deve ser maior que zero"
  }
}
```

### Erro de conversão (400)

```json
{
  "status": 400,
  "error": "Erro de Conversão de Tipo",
  "path": "/stocks",
  "fields": {
    "quantity": "Valor inválido para este campo"
  }
}
```

---

## Testes Unitários

87 testes cobrindo cenários de sucesso e falha em todas as camadas de serviço e controller.

```bash
mvn test
```

| Classe | Testes |
|---|---|
| `CategoryServiceTest` | 8 |
| `CategoryControllerTest` | 7 |
| `ProductServiceTest` | 11 |
| `ProductControllerTest` | 7 |
| `StockServiceTest` | 9 |
| `StockControllerTest` | 7 |
| `StockMovementServiceTest` | 10 |
| `SalesServiceTest` | 5 |
| `SaleControllerTest` | 6 |

---

## Frontend PDV

O frontend React está em `frontend-pdv/` e consome esta API.

```bash
cd frontend-pdv
npm install
npm run dev
```

Funcionalidades:
- **Vendas**: Carrinho, pagamento com troco, cadastro de produto
- **Produtos**: CRUD completo com busca e paginação
- **Categorias**: CRUD com confirmação de exclusão
- **Estoque**: Movimentações (ENTRY/EXIT/LOSS/ADJUSTMENT) com motivo
