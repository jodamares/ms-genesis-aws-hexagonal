# ms-organization-template

Reactive multi-module service for organization template operations.

## Modules

- `shared`: CQRS buses, idempotency abstractions, correlation context, and error handling.
- `src/template`: domain, application use cases, ports, and R2DBC adapters.
- `apps/backend`: Spring Boot WebFlux entrypoints, HTTP cache policy, and runtime configuration.

## Endpoints

- `GET /custom-path/templateorganization/v1/keylist?key={key}`
- `GET /custom-path/templateorganization/v1/settingsapp`
- `GET /custom-path/templateorganization/v1/widgets?productID={productId}&productPS={productPs}`

## HTTP cache

GET endpoints return `Cache-Control` headers. Disable local cache headers with `TEMPLATE_HTTP_CACHE_ENABLED=false`.

## Local run

```bash
docker compose up --build
```

## Tests

```bash
./mvnw -B -pl apps/backend -am test
```
