package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Ordeal.of() static method")
final class OrdealOfTest
{
	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for delegated ordeals")
	final class DelegatedOrdealTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("delegationCases")
		@DisplayName("should delegate endurance to the supplied function")
		void shouldDelegateEnduranceToTheSuppliedFunction(final String description,
		                                                  final Function<String, Integer> function,
		                                                  final String boon,
		                                                  final Integer expected) throws Exception
		{
			Ordeal<String, Integer> result = Ordeal.of(function);

			assertThat(result.endure(boon)).as("ordeal.of() should delegate for %s", description)
			                               .isEqualTo(expected);
		}

		private static Stream<Arguments> delegationCases()
		{
			return Stream.of(
								 new DelegationCase("string length function", String::length, "orpheus", 7),
								 new DelegationCase("arithmetical function", Integer::parseInt, "7", 7))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.function,
								 testCase.boon,
								 testCase.expected));
		}

		private record DelegationCase(String description,
		                              Function<String, Integer> function,
		                              String boon,
		                              Integer expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for invalid factory arguments")
	final class InvalidFactoryArgumentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidCases")
		@DisplayName("should reject null functions")
		void shouldRejectNullFunctions(final String description, final Invocation invocation)
		{
			assertThatThrownBy(invocation::invoke)
					.as("ordeal.of() should reject invalid argument for %s", description)
					.isInstanceOf(NullPointerException.class);
		}

		private static Stream<Arguments> invalidCases()
		{
			return Stream.of(
								 new InvalidCase("null function", () -> Ordeal.of(null)))
			             .map(testCase -> Arguments.of(testCase.description, testCase.invocation));
		}

		private record InvalidCase(String description, Invocation invocation)
		{
		}
	}
}