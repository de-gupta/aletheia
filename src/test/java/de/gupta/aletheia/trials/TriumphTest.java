package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for Triumph")
final class TriumphTest
{
	private static Fallible<String> fury()
	{
		return Fallible.beckon("echo")
		               .metamorphose(_ ->
					   {
						   throw new IllegalArgumentException("broken");
					   }, List.of());
	}

	@Nested
	@DisplayName("Tests for equals()")
	final class EqualityTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("identityCases")
		@DisplayName("should equal itself under identity comparison")
		void shouldEqualItselfUnderIdentityComparison(final String description, final Fallible<?> triumph)
		{
			assertThat(triumph.equals(triumph))
					.as("triumph should be equal to itself for: %s", description)
					.isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalCases")
		@DisplayName("should be equal when boon is the same value")
		void shouldBeEqualWhenBoonIsTheSameValue(final String description,
		                                         final Fallible<?> left,
		                                         final Fallible<?> right)
		{
			assertThat(left)
					.as("triumphs bearing the same boon should be equal for: %s", description)
					.isEqualTo(right);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("unequalCases")
		@DisplayName("should not be equal when boon or type differs")
		void shouldNotBeEqualWhenBoonOrTypeDiffers(final String description,
		                                           final Fallible<?> left,
		                                           final Object right)
		{
			assertThat(left)
					.as("triumph should not equal the other party for: %s", description)
					.isNotEqualTo(right);
		}

		private static Stream<Arguments> identityCases()
		{
			return Stream.of(
								 new IdentityCase("a string triumph", Fallible.beckon("Orpheus")),
								 new IdentityCase("an integer triumph", Fallible.beckon(42)),
								 new IdentityCase("a null triumph", Fallible.beckon(null)))
			             .map(tc -> Arguments.of(tc.description(), tc.triumph()));
		}

		private static Stream<Arguments> equalCases()
		{
			return Stream.of(
								 new EqualCase("string boon", Fallible.beckon("Orpheus"), Fallible.beckon("Orpheus")),
								 new EqualCase("integer boon", Fallible.beckon(42), Fallible.beckon(42)),
								 new EqualCase("null boon", Fallible.beckon(null), Fallible.beckon(null)))
			             .map(tc -> Arguments.of(tc.description(), tc.left(), tc.right()));
		}

		private static Stream<Arguments> unequalCases()
		{
			return Stream.of(
								 new UnequalCase("null", Fallible.beckon("echo"), null),
								 new UnequalCase("a fury", Fallible.beckon("echo"), fury()),
								 new UnequalCase("a triumph with a different string boon",
										 Fallible.beckon("Orpheus"), Fallible.beckon("Eurydice")),
								 new UnequalCase("a non-null triumph against a null triumph",
										 Fallible.beckon("echo"), Fallible.beckon(null)))
			             .map(tc -> Arguments.of(tc.description(), tc.left(), tc.right()));
		}

		private record IdentityCase(String description, Fallible<?> triumph)
		{
		}

		private record EqualCase(String description, Fallible<?> left, Fallible<?> right)
		{
		}

		private record UnequalCase(String description, Fallible<?> left, Object right)
		{
		}
	}

	@Nested
	@DisplayName("Tests for hashCode()")
	final class HashCodeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("hashConsistencyCases")
		@DisplayName("should return a stable hashCode across invocations")
		void shouldReturnAStableHashCodeAcrossInvocations(final String description, final Fallible<?> triumph)
		{
			assertThat(triumph.hashCode())
					.as("hashCode() should be stable across calls for: %s", description)
					.isEqualTo(triumph.hashCode());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalTriumphHashCases")
		@DisplayName("should produce equal hashCodes for equal triumphs")
		void shouldProduceEqualHashCodesForEqualTriumphs(final String description,
		                                                 final Fallible<?> left,
		                                                 final Fallible<?> right)
		{
			assertThat(left.hashCode())
					.as("equal triumphs should share the same hashCode for: %s", description)
					.isEqualTo(right.hashCode());
		}

		private static Stream<Arguments> hashConsistencyCases()
		{
			return Stream.of(
								 new HashConsistencyCase("a string triumph", Fallible.beckon("Orpheus")),
								 new HashConsistencyCase("an integer triumph", Fallible.beckon(42)),
								 new HashConsistencyCase("a null triumph", Fallible.beckon(null)))
			             .map(tc -> Arguments.of(tc.description(), tc.triumph()));
		}

		private static Stream<Arguments> equalTriumphHashCases()
		{
			return Stream.of(
								 new EqualHashCase("string boons", Fallible.beckon("Orpheus"), Fallible.beckon("Orpheus")),
								 new EqualHashCase("null boons", Fallible.beckon(null), Fallible.beckon(null)))
			             .map(tc -> Arguments.of(tc.description(), tc.left(), tc.right()));
		}

		private record HashConsistencyCase(String description, Fallible<?> triumph)
		{
		}

		private record EqualHashCase(String description, Fallible<?> left, Fallible<?> right)
		{
		}
	}
}