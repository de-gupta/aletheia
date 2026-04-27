# Test Style Guide

Tests are executable specifications. Every test should read like a truth statement about the system, not a procedure.
The structure encodes intent; annotations and names are the only documentation needed.

---

## Structure — subject / context / behaviour

```
outer class  = subject under test
nested class = context (when / given / for)
test method  = observable behaviour
```

```java

@DisplayName("Unfolding#metamorphose")
final class UnfoldingMetamorphoseTest
{

	@Nested
	@DisplayName("when value is present")
	final class WhenValueIsPresent
	{

		@Test
		@DisplayName("applies the function and returns the transformed value")
		void appliesTheFunctionAndReturnsTheTransformedValue()
		{ ...}

		@Test
		@DisplayName("returns empty when the function produces null")
		void returnsEmptyWhenFunctionProducesNull()
		{ ...}
	}

	@Nested
	@DisplayName("when Unfolding is empty")
	final class WhenUnfoldingIsEmpty
	{

		@Test
		@DisplayName("returns empty without invoking the function")
		void returnsEmptyWithoutInvokingTheFunction()
		{ ...}
	}
}
```

Rules:

- One file per method under test (or one file per type for small types).
- Top-level class and all nested classes: **`final`**, package-private, no `Test` suffix on nested classes.
- Nested class names are context clauses: `WhenPresent`, `WhenEmpty`, `WhenMetamorphosisThrows`, `WithNullArguments`.
- Test method names are full behaviour sentences in `camelCase`, no `test` prefix.

---

## Given-When-Then (structural, not commented)

Encode the three phases through variable names and blank lines — never with `// given / // when / // then` comments.

```java

@Test
@DisplayName("applies discount for premium customers")
void appliesDiscountForPremiumCustomers()
{
	var order = premiumOrderOf(100);

	var result = pricing.calculate(order);

	assertThat(result).hasTotal(90);
}
```

When setup is non-trivial, extract to a private helper named to explain *what* it produces, not *how*:

```java
private static Fallible<String> furyWith(String message)
{
	return Fallible.beckon("echo")
	               .metamorphose(_ ->
	               {
		               throw new IllegalArgumentException(message);
	               }, List.of());
}
```

Helpers for shared setup belong at the **outer** test class level; helpers used only inside one nested class belong
there.

---

## Parameterized tests

```java

@ParameterizedTest(name = "{0}")
@MethodSource("presentValueCases")
@DisplayName("applies the function and returns the transformed value")
<T, R> void appliesTheFunctionAndReturnsTheTransformedValue(final String as, final TransformCase<T, R> tc)
{ ...}
```

- `name = "{0}"` — the first argument is always the human label, shown in the runner.
- `@DisplayName` on the method is the invariant; `{0}` is the concrete scenario.
- Non-parameterized edge cases live as `@Test` methods alongside in the same nested class.
- The method source has the **same name** as the test method, suffixed `Cases`.

---

## `as` — the description convention

The human label is always named **`as`** in records and parameters, and is always the **first** argument to
`Arguments.of`.

```java
assertThat(actual)
    .

as("metamorphose() on %s with %s should yield %s",source, fn, expected)
    .

usingRecursiveComparison()
    .

isEqualTo(expected);
```

Always include `.as(pattern, args...)` on assertions. Use `%s` format specifiers, not string concatenation.

---

## Three `@MethodSource` patterns

### Pattern A — direct `Arguments.of`

Use when all arguments are simple types or lambdas that need no reuse.

```java
private static Stream<Arguments> transformCases()
{
	return Stream.of(
			Arguments.of("String to length", Unfolding.beckon("hello"), (Function<String, Integer>) String::length,
					Unfolding.beckon(5)),
			Arguments.of("Integer to String", Unfolding.beckon(42), (Function<Integer, String>) Object::toString,
					Unfolding.beckon("42"))
	);
}
```

### Pattern B — record as single test parameter, mapped to `Arguments.of(tc.as(), tc)`

Use when the test method receives the record as one typed argument. The record carries all data; the stream extracts
only `as` + the record.

```java
private record TransformCase<T, R>(String as, Unfolding<T> source, Function<T, R> fn, Unfolding<R> expected)
{
	private static <T, R> TransformCase<T, R> of(String as, Unfolding<T> source, Function<T, R> fn,
	                                             Unfolding<R> expected)
	{
		return new TransformCase<>(as, source, fn, expected);
	}
}

private static Stream<Arguments> transformCases()
{
	return Stream.of(
			TransformCase.of("String to length", Unfolding.beckon("hello"), (Function<String, Integer>) String::length,
					Unfolding.beckon(5)),
			TransformCase.of("Integer to String", Unfolding.beckon(42), (Function<Integer, String>) Object::toString,
					Unfolding.beckon("42"))
	).map(tc -> Arguments.of(tc.as(), tc));
}

// test signature:
<T, R> void appliesTheFunction(final String as, final TransformCase<T, R> tc)
{ ...}
```

### Pattern C — record fields individually spread into `Arguments.of`

Use when the test method receives primitive arguments and a record wrapper would add no clarity.

```java
private record IdentityCase(String as, Fallible<String> fury)
{
}

private static Stream<Arguments> identityCases()
{
	return Stream.of(
			new IdentityCase("fury with illegal argument", furyWith("broken")),
			new IdentityCase("fury with different message", furyWith("cursed"))
	).map(tc -> Arguments.of(tc.as(), tc.fury()));
}

// test signature:
void shouldEqualItself(final String as, final Fallible<String> fury)
{ ...}
```

---

## Records

- Declared `private record` inside the nested class that uses them.
- Named after the scenario shape: `TransformCase`, `IdentityCase`, `EqualCase`, `NullGuardCase`.
- Include a static factory named `of(...)` or `shape(...)` when generic type parameters are involved — avoids unchecked
  inference.
- Never expose more fields than the test needs.
- Override `toString()` when the record itself is the `{0}` argument (Pattern B) — the default component list is usually
  enough, but make it readable.

---

## Functional interfaces

Declare a `private @FunctionalInterface` inside the nested class when you need a throwing lambda inside a record or
`Arguments`:

```java

@FunctionalInterface
private interface Invocation
{
	void invoke();
}

private record NullGuardCase(String as, Invocation invocation)
{
}
```

---

## Assertions

**Never** extract a boolean from a domain object and assert `isTrue()` / `isFalse()` — that loses type information and
produces useless failure messages. Always assert on the full observable outcome.

| Situation                      | Preferred style                                                                      |
|--------------------------------|--------------------------------------------------------------------------------------|
| Value / structural equality    | `assertThat(actual).as(...).usingRecursiveComparison().isEqualTo(expected)`          |
| Simple value equality          | `assertThat(actual).as(...).isEqualTo(expected)`                                     |
| Presence / absence             | `assertThat(actual).as(...).usingRecursiveComparison().isEqualTo(Unfolding.chaos())` |
| Exception thrown               | `assertThatThrownBy(() -> ...).as(...).isInstanceOf(...).hasMessage(...)`            |
| Exception cause                | chain `.cause().isInstanceOf(...).hasMessage(...)`                                   |
| Multiple facts about one value | `assertThat(actual).satisfies(u -> { ...; ... })`                                    |

Use AssertJ only (`assertThat`, `assertThatThrownBy`). Never JUnit `assertEquals` / `assertTrue`.

### Custom domain assertions (aspire to)

For core types tested extensively, build a fluent `AbstractAssert` subclass:

```java
class UnfoldingAssert<T> extends AbstractAssert<UnfoldingAssert<T>, Unfolding<T>>
{

	static <T> UnfoldingAssert<T> assertThatUnfolding(Unfolding<T> actual)
	{
		return new UnfoldingAssert<>(actual);
	}

	UnfoldingAssert<T> isSterile()
	{
		isNotNull();
		if (!actual.sterile()) failWithMessage("Expected empty Unfolding but was <%s>", actual);
		return this;
	}

	UnfoldingAssert<T> hasValue(T expected)
	{
		isNotNull();
		assertThat(actual.summon()).as("value").isEqualTo(expected);
		return this;
	}
}
```

Tests then read as domain statements:

```java
assertThatUnfolding(result).

isSterile();

assertThatUnfolding(result).

hasValue("expected");
```

---

## Contract tests for sealed types

When a sealed interface has multiple implementations, define a `default`-method test interface and implement it once per
variant. This guarantees all implementations satisfy the same contract.

```java
interface UnfoldingContract<T>
{

	Unfolding<T> present(T value);

	Unfolding<T> empty();

	@Test
	default void metamorphoseOnEmptyReturnsEmpty()
	{
		assertThat(empty().metamorphose(Function.identity()))
				.usingRecursiveComparison()
				.isEqualTo(empty());
	}
}

final class ShellContractTest implements UnfoldingContract<String>
{
	public Unfolding<String> present(String v)
	{
		return Unfolding.beckon(v);
	}

	public Unfolding<String> empty()
	{
		return Unfolding.chaos();
	}
}
```

---

## Dynamic tests (`@TestFactory`)

Use `@TestFactory` with `DynamicTest` when test cases are generated programmatically or when the scenario label must be
computed at runtime (e.g., combinatoric edge cases):

```java

@TestFactory
Stream<DynamicTest> rejectsAllInvalidInputs()
{
	return Stream.of("", " ", null, "\t")
	             .map(input -> dynamicTest("input: " + input, () ->
						 assertThatThrownBy(() -> parse(input))
								 .isInstanceOf(IllegalArgumentException.class)));
}
```

Prefer `@ParameterizedTest` when cases are static data; prefer `@TestFactory` when the test descriptions or inputs must
be computed.

---

## Property-based testing (advanced)

For pure functions over unconstrained input spaces, add a jqwik dependency and write properties alongside example-based
tests:

```java

@Property
void metamorphoseNeverThrowsOnArbitraryStrings(@ForAll String input)
{
	Unfolding.beckon(input).metamorphose(String::length);
}
```

Properties complement — they do not replace — example-based tests.

---

## Avoiding over-abstraction

> If you have to jump files or scroll through helpers to understand a single test, you have gone too far.

- Keep records, interfaces, and helpers inside the nested class that needs them.
- If a helper is used by more than two nested classes, promote it to the outer class — not to a separate file.
- Prefer a slightly repetitive test over an abstraction that obscures what is being tested.

---

## Conventions at a glance

| Rule                  | Detail                                                             |
|-----------------------|--------------------------------------------------------------------|
| Test class modifier   | `final`, package-private                                           |
| Nested class modifier | `@Nested @DisplayName`, `final`, package-private, no `Test` suffix |
| Nested class naming   | context clause: `WhenPresent`, `WhenEmpty`, `WithNullArguments`    |
| Test method naming    | full behaviour sentence, no `test` prefix, `camelCase`             |
| Parameterized name    | always `"{0}"`                                                     |
| Description parameter | always named `as`                                                  |
| Method source name    | same name as the test method + `Cases` suffix                      |
| Record factory        | `of(...)` or `shape(...)` when generics present                    |
| Boolean assertions    | never `isTrue()`/`isFalse()` on domain boolean accessors           |
| Assertions library    | AssertJ only — `assertThat`, `assertThatThrownBy`                  |
| JUnit assertions      | never                                                              |