# Contrato de paginação para o front-end

## Objetivo

Este documento especifica como a API deve publicar respostas paginadas no
OpenAPI para que o Orval gere tipos TypeScript completos e seguros.

Atualmente, os endpoints paginados apontam para um único schema
`#/components/schemas/PagedModel`, cujo `content.items` é apenas `object`. Como
consequência, o Orval gera:

```ts
export interface PagedModel {
  content?: Record<string, unknown>[]
  page?: PageMetadata
}
```

O tipo real de cada item é perdido. O contrato deve informar explicitamente,
por endpoint, se `content` contém `ClienteDTO`, `ProcessoDTO`,
`FaturamentoDTO` ou `UsuarioResponseDTO`.

## Formato JSON obrigatório

Todas as listagens paginadas devem manter a mesma estrutura:

```json
{
  "content": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

Regras:

- `content` é obrigatório e nunca deve ser `null`; para uma página vazia, usar
  `[]`.
- `page` é obrigatório e nunca deve ser `null`.
- `number` é baseado em zero: a primeira página é `0`.
- `size`, `number` e `totalPages` são inteiros não negativos.
- `totalElements` é um inteiro longo não negativo.
- Os mesmos nomes e significados devem ser usados em todos os endpoints.
- A resposta de sucesso deve declarar `application/json`, não apenas `*/*`.
- Parâmetros de ordenação devem seguir `sort=campo,direcao`; para múltiplas
  ordenações, repetir `sort`.

## Schemas concretos necessários

Cada recurso deve ter um schema paginado próprio:

| Endpoint | Schema da resposta | Tipo de `content.items` |
| --- | --- | --- |
| `GET /api/clientes` | `ClientePageResponse` | `ClienteDTO` |
| `GET /api/processos` | `ProcessoPageResponse` | `ProcessoDTO` |
| `GET /api/processos/cliente/{clienteId}` | `ProcessoPageResponse` | `ProcessoDTO` |
| `GET /api/faturamento` | `FaturamentoPageResponse` | `FaturamentoDTO` |
| `GET /api/usuarios` | `UsuarioPageResponse` | `UsuarioResponseDTO` |

Novos endpoints paginados devem seguir a mesma convenção:
`<Recurso>PageResponse`.

## Implementação recomendada em Spring Boot

### Metadados compartilhados

```java
package com.seuprojeto.api.pagination;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PageMetadata", description = "Metadados da paginação baseada em zero")
public record PageMetadataDTO(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", example = "20")
    int size,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", example = "0")
    int number,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", example = "125")
    long totalElements,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", example = "7")
    int totalPages
) {}
```

### Resposta concreta para clientes

Prefira um DTO concreto por recurso. Isso evita que o Springdoc perca o tipo
genérico devido ao apagamento de tipos da JVM.

```java
package com.seuprojeto.api.cliente;

import com.seuprojeto.api.pagination.PageMetadataDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "ClientePageResponse")
public record ClientePageResponse(
    @ArraySchema(
        schema = @Schema(implementation = ClienteDTO.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    List<ClienteDTO> content,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PageMetadataDTO page
) {
    public static ClientePageResponse from(Page<ClienteDTO> result) {
        return new ClientePageResponse(
            result.getContent(),
            new PageMetadataDTO(
                result.getSize(),
                result.getNumber(),
                result.getTotalElements(),
                result.getTotalPages()
            )
        );
    }
}
```

Criar os equivalentes `ProcessoPageResponse`, `FaturamentoPageResponse` e
`UsuarioPageResponse`, alterando somente o tipo da lista e o nome do schema.

### Controller

O tipo concreto deve aparecer tanto na assinatura quanto na documentação da
resposta:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Operation(summary = "Listar clientes com paginação e filtros")
@ApiResponse(
    responseCode = "200",
    description = "Página de clientes obtida com sucesso",
    content = @Content(
        mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = ClientePageResponse.class)
    )
)
public ResponseEntity<ClientePageResponse> listar(
    @ParameterObject Pageable pageable,
    @RequestParam(required = false) String q
) {
    Page<ClienteDTO> result = clienteService.listar(pageable, q);
    return ResponseEntity.ok(ClientePageResponse.from(result));
}
```

O mesmo padrão deve ser aplicado aos demais controllers. Não retornar
`PagedModel` sem parâmetro de tipo, `Page<?>`, `Object`, `Map<String, Object>` ou
um wrapper genérico que resulte em `content.items: { type: object }` no
OpenAPI.

## OpenAPI esperado

Após a alteração, o `/v3/api-docs` deve conter uma estrutura equivalente a:

```yaml
components:
  schemas:
    PageMetadata:
      type: object
      required:
        - size
        - number
        - totalElements
        - totalPages
      properties:
        size:
          type: integer
          format: int32
          minimum: 1
        number:
          type: integer
          format: int32
          minimum: 0
        totalElements:
          type: integer
          format: int64
          minimum: 0
        totalPages:
          type: integer
          format: int32
          minimum: 0

    ClientePageResponse:
      type: object
      required:
        - content
        - page
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/ClienteDTO'
        page:
          $ref: '#/components/schemas/PageMetadata'
```

E a resposta do endpoint:

```yaml
paths:
  /api/clientes:
    get:
      responses:
        '200':
          description: Página de clientes obtida com sucesso
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ClientePageResponse'
```

## Resultado esperado no Orval

Depois de executar `npm run api:generate` no front-end, o Orval deve gerar:

```ts
export interface ClientePageResponse {
  content: ClienteDTO[]
  page: PageMetadata
}

export interface ProcessoPageResponse {
  content: ProcessoDTO[]
  page: PageMetadata
}
```

As funções geradas devem retornar diretamente esses tipos:

```ts
listarClientes(params): Promise<ClientePageResponse>
listarProcessos(params): Promise<ProcessoPageResponse>
```

Não deve mais existir `PagedModelContentItem = Record<string, unknown>` para
essas operações.

## Critérios de aceite

- [ ] Cada endpoint paginado referencia um schema concreto no OpenAPI.
- [ ] Cada `content.items` possui `$ref` para o DTO correto.
- [ ] `content` e `page` estão em `required`.
- [ ] Respostas vazias retornam `content: []` e metadados numéricos válidos.
- [ ] A resposta `200` declara `application/json`.
- [ ] `GET /api/processos/cliente/{clienteId}` reutiliza
      `ProcessoPageResponse`.
- [ ] O JSON real corresponde ao schema publicado no `/v3/api-docs`.
- [ ] `npm run api:generate` gera listas tipadas, sem
      `PagedModelContentItem` genérico.
- [ ] `npm run build` do front-end passa sem conversões manuais de
      `Record<string, unknown>`.

## Observação sobre `PagedModel`

O `org.springframework.data.web.PagedModel<T>` fornece uma representação JSON
estável para `Page<T>`, e o `org.springframework.hateoas.PagedModel<T>` mantém
informações de paginação com suporte a links. Ambos podem ser usados em
runtime, mas o OpenAPI precisa preservar o tipo concreto de `T`. Como o sistema
não depende atualmente de links HATEOAS, DTOs concretos de resposta são a
alternativa mais previsível para clientes gerados.

## Referências oficiais

- [Spring Data `PagedModel<T>`](https://docs.spring.io/spring-data/commons/reference/api/java/org/springframework/data/web/PagedModel.html)
- [Spring HATEOAS `PagedModel<T>`](https://docs.spring.io/spring-hateoas/docs/current/api/org/springframework/hateoas/PagedModel.html)
- [Spring Boot: suporte a Spring HATEOAS](https://docs.spring.io/spring-boot/reference/web/spring-hateoas.html)
