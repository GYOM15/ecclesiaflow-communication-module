# Test Plan

## Scope

## Test types

- Unit tests (business rules, mappers default methods)
- Slice tests (web, persistence) only when needed
- Integration tests (DB/AMQP/gRPC) for critical flows

## Coverage priorities

1. Business services (provider selection, retries)
2. Template rendering
3. REST delegate + exception handling
4. Persistence adapter
5. Messaging adapter
