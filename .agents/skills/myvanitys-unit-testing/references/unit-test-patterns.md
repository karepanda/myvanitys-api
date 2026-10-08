# Unit-Test Patterns

Read only the section that matches the unit under test. These patterns describe project conventions, not templates that must be copied verbatim.

## Shared shape

- Keep the test in the same package as the production type.
- Prefer fixed constants for identity-sensitive UUIDs and timestamps.
- Use Given/When/Then spacing and name the observable behavior.
- Prefer real domain values and small factory helpers over deep mocks.
- Assert output/state first, then verify only meaningful collaborator effects.
- Cover calls that must not occur on rejected or unauthorized paths.

## Domain models and value objects

Do not use Mockito. Construct the real object and test invariants, state transitions, equality semantics, and domain exceptions. Parameterize repeated validation boundaries. Avoid testing getters unless their behavior is non-trivial.

```java
@ParameterizedTest
@MethodSource("invalidColors")
void create_whenColorIsInvalid_thenThrowsProductValidationException(String color) {
  assertThatThrownBy(() -> Product.create("Lipstick", "Brand", category, color))
      .isInstanceOf(ProductValidationException.class);
}
```

## Application use cases

Mock secondary ports and keep the use case real. Test returned domain state, domain/application exceptions, saved arguments, and forbidden persistence.

```java
@ExtendWith(MockitoExtension.class)
class AddProductToMyVanityTest {

  @Mock ProductRepository productRepository;
  @Mock ProductUserRepository productUserRepository;
  @InjectMocks AddProductToMyVanity target;

  @Test
  void execute_whenProductDoesNotExist_thenThrowsProductNotFoundException() {
    when(productUserRepository.existsByProductIdAndUserId(productId, userId))
        .thenReturn(false);
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> target.execute(command))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessage("Product does not exist");

    verify(productUserRepository, never())
        .saveProductUserRelationship(any(), any());
  }
}
```

Do not add duplicate tests for a generic runtime exception and a transaction rollback when both merely prove unchanged exception propagation.

## Primary adapters and controllers

Mock primary ports and response mappers. Use a real `AuthenticatedUserContext` when authentication state is part of the behavior. Capture commands to prove the controller used the JWT-derived user instead of client data.

```java
context.setUserId(AUTHENTICATED_USER_ID);

controller.addProductToUserVanity(PRODUCT_ID, REQUEST_ID, FLOW_ID, "en-US", USER_AGENT);

ArgumentCaptor<AddProductToMyVanityCommand> captor =
    ArgumentCaptor.forClass(AddProductToMyVanityCommand.class);
verify(addProductToMyVanityUseCase).execute(captor.capture());
assertThat(captor.getValue().userId()).isEqualTo(AUTHENTICATED_USER_ID);
```

Keep HTTP binding/validation and full exception-handler behavior in controller integration tests when direct invocation cannot prove them.

## Secondary adapters

For an adapter unit test, mock the external client or JPA repository and real/controlled mappers as appropriate. Assert translation between domain and infrastructure errors and verify exact persistence or provider arguments only when they are part of the adapter contract.

Do not treat a mocked JPA repository test as proof that a query, constraint, cascade, or mapping works in PostgreSQL; add an `*IT` for that behavior.

## Reactive authentication services

Use `StepVerifier` and keep the publisher lazy. Test success, empty results, and mapped errors without calling `block()`.

```java
StepVerifier.create(target.authenticateWithGoogle(command, requestId, flowId))
    .assertNext(session -> assertThat(session.user().getId()).isEqualTo(userId))
    .verifyComplete();
```

Use `expectErrorSatisfies` when the exception type and message or cause are meaningful.

## JWT and security mapping

Assert every security-sensitive claim independently. Do not rely on whole-object equality when a domain `equals` implementation compares only an ID. Use fixed `Instant` values and assert epoch seconds versus milliseconds explicitly.

Test unauthorized paths for both the thrown error/response and the absence of downstream use-case calls.

## AI image-analysis use cases

Unit tests must prove:

- The sanitizer and temporary storage receive the authenticated user's image.
- The analyzer receives the repository-provided category set.
- Suggestions are normalized and validated before returning.
- Unknown categories cannot escape as valid suggestions.
- Duplicate matching uses normalized name plus brand.
- The result contains an owned temporary reference.
- Product save, product-user save, and permanent image promotion have zero interactions during analysis.
- Provider throttling, failure, timeout, and invalid structured output map to application failures without sleeping.

Use fake byte content or a small non-personal fixture at the sanitizer boundary. Detailed decoder behavior belongs to sanitizer adapter tests.
