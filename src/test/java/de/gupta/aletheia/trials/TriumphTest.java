package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Triumph")
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
	@DisplayName("equals()")
	final class Equality
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("equalsItselfCases")
		@DisplayName("equals itself under identity comparison")
		void equalsItself(final String as, final Fallible<?> triumph)
		{
			assertThat(triumph)
					.as("triumph should equal itself for: %s", as)
					.isEqualTo(triumph);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalsAnotherTriumphWithTheSameBoonCases")
		@DisplayName("equals another triumph carrying the same boon")
		void equalsAnotherTriumphWithTheSameBoon(final String as, final EqualCase tc)
		{
			assertThat(tc.left())
					.as("triumphs bearing the same boon should be equal for: %s", as)
					.isEqualTo(tc.right());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("doesNotEqualWhenBoonOrTypeDiffersCases")
		@DisplayName("does not equal a triumph with a different boon, or a value of a different type")
		void doesNotEqualWhenBoonOrTypeDiffers(final String as, final InequalCase tc)
		{
			assertThat(tc.left())
					.as("triumph should not equal %s for: %s", tc.right(), as)
					.isNotEqualTo(tc.right());
		}

		private static Stream<Arguments> equalsItselfCases()
		{
			return Stream.of(
					new IdentityCase("a string triumph", Fallible.beckon("Orpheus")),
					new IdentityCase("an integer triumph", Fallible.beckon(42)),
					new IdentityCase("a null triumph", Fallible.beckon(null))
			).map(tc -> Arguments.of(tc.as(), tc.triumph()));
		}

		private static Stream<Arguments> equalsAnotherTriumphWithTheSameBoonCases()
		{
			return Stream.of(
					new EqualCase("string boon", Fallible.beckon("Orpheus"), Fallible.beckon("Orpheus")),
					new EqualCase("integer boon", Fallible.beckon(42), Fallible.beckon(42)),
					new EqualCase("null boon", Fallible.beckon(null), Fallible.beckon(null))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> doesNotEqualWhenBoonOrTypeDiffersCases()
		{
			return Stream.of(
					new InequalCase("null", Fallible.beckon("echo"), null),
					new InequalCase("a fury", Fallible.beckon("echo"), fury()),
					new InequalCase("a triumph with a different boon", Fallible.beckon("Orpheus"),
							Fallible.beckon("Eurydice")),
					new InequalCase("a non-null triumph against a null triumph", Fallible.beckon("echo"),
							Fallible.beckon(null))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record IdentityCase(String as, Fallible<?> triumph)
		{
		}

		private record EqualCase(String as, Fallible<?> left, Fallible<?> right)
		{
		}

		private record InequalCase(String as, Fallible<?> left, Object right)
		{
		}
	}

	@Nested
	@DisplayName("hashCode()")
	final class HashCode
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("returnsTheSameValueAcrossRepeatedCallsCases")
		@DisplayName("returns the same value across repeated calls")
		void returnsTheSameValueAcrossRepeatedCalls(final String as, final Fallible<?> triumph)
		{
			assertThat(triumph.hashCode())
					.as("hashCode() should be stable across calls for: %s", as)
					.isEqualTo(triumph.hashCode());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("isEqualForTriumphsThatCompareEqualCases")
		@DisplayName("is equal for triumphs that compare equal")
		void isEqualForTriumphsThatCompareEqual(final String as, final HashEqualCase tc)
		{
			assertThat(tc.left().hashCode())
					.as("equal triumphs should share the same hashCode for: %s", as)
					.isEqualTo(tc.right().hashCode());
		}

		private static Stream<Arguments> returnsTheSameValueAcrossRepeatedCallsCases()
		{
			return Stream.of(
					new StabilityCase("a string triumph", Fallible.beckon("Orpheus")),
					new StabilityCase("an integer triumph", Fallible.beckon(42)),
					new StabilityCase("a null triumph", Fallible.beckon(null))
			).map(tc -> Arguments.of(tc.as(), tc.triumph()));
		}

		private static Stream<Arguments> isEqualForTriumphsThatCompareEqualCases()
		{
			return Stream.of(
					new HashEqualCase("string boons", Fallible.beckon("Orpheus"), Fallible.beckon("Orpheus")),
					new HashEqualCase("null boons", Fallible.beckon(null), Fallible.beckon(null))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		@Test
		@DisplayName("hashCode() delegates to Objects.hashCode of the wrapped boon")
		void hashCodeDelegatesToWrappedBoon()
		{
			assertThat(Fallible.beckon("Orpheus").hashCode()).isEqualTo(Objects.hashCode("Orpheus"));
			assertThat(Fallible.beckon(42).hashCode()).isEqualTo(Objects.hashCode(42));
		}

		private record StabilityCase(String as, Fallible<?> triumph)
		{
		}

		private record HashEqualCase(String as, Fallible<?> left, Fallible<?> right)
		{
		}
	}
}