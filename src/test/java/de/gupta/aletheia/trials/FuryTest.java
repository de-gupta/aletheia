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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Fury")
final class FuryTest
{
	private static Fallible<String> fury(final String message)
	{
		return Fallible.beckon("echo")
		               .metamorphose(_ ->
					   {
						   throw new IllegalArgumentException(message);
					   }, List.of());
	}

	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for equals()")
	final class EqualityTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("identityCases")
		@DisplayName("should equal itself under identity comparison")
		void shouldEqualItselfUnderIdentityComparison(final String description, final Fallible<String> fury)
		{
			assertThat(fury.equals(fury))
					.as("fury should be equal to itself for: %s", description)
					.isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalCases")
		@DisplayName("should be equal when wrapping the same doom instance")
		void shouldBeEqualWhenWrappingTheSameDoomInstance(final String description,
		                                                  final Fallible<String> left,
		                                                  final Fallible<String> right)
		{
			assertThat(left)
					.as("furies wrapping the same doom instance should be equal for: %s", description)
					.isEqualTo(right);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("unequalCases")
		@DisplayName("should not be equal when doom or type differs")
		void shouldNotBeEqualWhenDoomOrTypeDiffers(final String description,
		                                           final Fallible<String> left,
		                                           final Object right)
		{
			assertThat(left)
					.as("fury should not equal the other party for: %s", description)
					.isNotEqualTo(right);
		}

		private static Stream<Arguments> identityCases()
		{
			return Stream.of(
								 new IdentityCase("a fury bearing an illegal argument", fury("broken")),
								 new IdentityCase("a fury bearing a different message", fury("cursed")))
			             .map(tc -> Arguments.of(tc.description(), tc.fury()));
		}

		private static Stream<Arguments> equalCases()
		{
			final IllegalArgumentException sharedDoom = new IllegalArgumentException("shared");

			return Stream.of(
								 new EqualCase(
										 "the same doom instance should yield equal furies regardless of their boon origin",
										 Fallible.beckon("echo").metamorphose(_ ->
										 {
											 throw sharedDoom;
										 }, List.of()),
										 Fallible.beckon("echo").metamorphose(_ ->
										 {
											 throw sharedDoom;
										 }, List.of())))
			             .map(tc -> Arguments.of(tc.description(), tc.left(), tc.right()));
		}

		private static Stream<Arguments> unequalCases()
		{
			return Stream.of(
								 new UnequalCase("null", fury("broken"), null),
								 new UnequalCase("a triumph bearing the same text as the doom message",
										 fury("broken"), Fallible.beckon("broken")),
								 new UnequalCase("a fury with a different doom instance",
										 fury("alpha"), fury("omega")))
			             .map(tc -> Arguments.of(tc.description(), tc.left(), tc.right()));
		}

		private record IdentityCase(String description, Fallible<String> fury)
		{
		}

		private record EqualCase(String description, Fallible<String> left, Fallible<String> right)
		{
		}

		private record UnequalCase(String description, Fallible<String> left, Object right)
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
		void shouldReturnAStableHashCodeAcrossInvocations(final String description, final Fallible<String> fury)
		{
			assertThat(fury.hashCode())
					.as("hashCode() should be stable across calls for: %s", description)
					.isEqualTo(fury.hashCode());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalFuryHashCases")
		@DisplayName("should produce equal hashCodes for equal furies")
		void shouldProduceEqualHashCodesForEqualFuries(final String description,
		                                               final Fallible<String> left,
		                                               final Fallible<String> right)
		{
			assertThat(left.hashCode())
					.as("equal furies should share the same hashCode for: %s", description)
					.isEqualTo(right.hashCode());
		}

		private static Stream<Arguments> hashConsistencyCases()
		{
			return Stream.of(
								 new HashConsistencyCase("a fury bearing an illegal argument", fury("doom")),
								 new HashConsistencyCase("a fury bearing a different message", fury("broken")))
			             .map(tc -> Arguments.of(tc.description(), tc.fury()));
		}

		private static Stream<Arguments> equalFuryHashCases()
		{
			final IllegalArgumentException sharedDoom = new IllegalArgumentException("shared");

			return Stream.of(
								 new EqualHashCase(
										 "furies wrapping the same doom instance",
										 Fallible.beckon("a").metamorphose(_ ->
										 {
											 throw sharedDoom;
										 }, List.of()),
										 Fallible.beckon("b").metamorphose(_ ->
										 {
											 throw sharedDoom;
										 }, List.of())))
			             .map(tc -> Arguments.of(tc.description(), tc.left(), tc.right()));
		}

		@Test
		@DisplayName("hashCode() delegates to Objects.hashCode of the wrapped doom")
		void hashCodeDelegatesToWrappedDoom()
		{
			final IllegalArgumentException doom = new IllegalArgumentException("hashtest");
			final Fallible<String> fury = Fallible.beckon("x").metamorphose(_ ->
			{
				throw doom;
			}, List.of());
			assertThat(fury.hashCode()).isEqualTo(Objects.hashCode(doom));
		}

		private record HashConsistencyCase(String description, Fallible<String> fury)
		{
		}

		private record EqualHashCase(String description, Fallible<String> left, Fallible<String> right)
		{
		}
	}

	@Nested
	@DisplayName("Tests for null doom guard")
	final class NullDoomTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("nullDoomCases")
		@DisplayName("should reject null doom on construction")
		void shouldRejectNullDoomOnConstruction(final String description, final Invocation invocation)
		{
			assertThatThrownBy(invocation::invoke)
					.as("Fury.arise() should reject null doom for: %s", description)
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("doom may not be null");
		}

		private static Stream<Arguments> nullDoomCases()
		{
			return Stream.of(
								 new NullDoomCase("direct construction via arise()", () -> Fury.arise(null)))
			             .map(tc -> Arguments.of(tc.description(), tc.invocation()));
		}

		private record NullDoomCase(String description, Invocation invocation)
		{
		}
	}
}