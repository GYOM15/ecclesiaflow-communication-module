# Tests matrix

## Current

- Existing tests:
  - `EmailTest`
  - `EmailServiceImplTest`

## Next (priority)

- `ThymeleafTemplateServiceTest` (unit)
- `EmailQueueProcessorTest` (unit)
- `EmailApiDelegateImplTest` (unit, Mockito)
- `GlobalExceptionHandlerTest` (unit)

## Later

- Persistence adapter tests (`EmailRepositoryImpl` with H2/Testcontainers)
- Messaging tests (`EmailMessageListener` with mocked `EmailService`)
- Provider tests (Gmail/SendGrid) using mocks and/or stub transport
- gRPC server tests (`EmailGrpcServiceImpl`) with grpc-testing
