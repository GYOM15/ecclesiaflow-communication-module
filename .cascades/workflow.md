# Workflow

## Working agreement

### Feature work

1. Define scope and acceptance criteria
2. Identify impacted layers (business, io, web, templates)
3. Implement with SRP/SOLID and small commits
4. Add/adjust tests
5. Run `mvn test`
6. Run `mvn -q spotless:check` / formatting checks if configured
7. Final review checklist

### Review checklist

- Code compiles (`mvn -q clean compile`)
- Tests pass (`mvn -q test`)
- No dead code or commented-out code
- Error handling is explicit and logged at boundaries
- Config defaults are safe (prod vs dev)

## Definition of done

- Feature meets acceptance criteria
- Tests cover critical paths and edge cases
- Documentation updated if behavior changed
