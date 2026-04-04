package de.gupta.aletheia.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Triplet tests")
final class TripletTest
{
	@Nested
	@DisplayName("Factory and aliases")
	final class FactoryAndAliasesTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("directFactoryCases")
		@DisplayName("of(a, b, c) should preserve values")
		<A, B, C> void ofShouldPreserveValues(final String as, final DirectFactoryCase<A, B, C> tc)
		{
			var triplet = Triplet.of(tc.first(), tc.second(), tc.third());

			assertThat(triplet.first()).as("first should match for %s", as).isEqualTo(tc.first());
			assertThat(triplet.second()).as("second should match for %s", as).isEqualTo(tc.second());
			assertThat(triplet.third()).as("third should match for %s", as).isEqualTo(tc.third());
			assertThat(triplet.left()).as("left alias should match for %s", as).isEqualTo(tc.first());
			assertThat(triplet.center()).as("center alias should match for %s", as).isEqualTo(tc.second());
			assertThat(triplet.right()).as("right alias should match for %s", as).isEqualTo(tc.third());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("pairFactoryCases")
		@DisplayName("of(a, pair) should expand pair into second and third")
		<A, B, C> void ofWithPairShouldExpandPair(final String as, final PairFactoryCase<A, B, C> tc)
		{
			var triplet = Triplet.of(tc.first(), tc.pair());

			assertThat(triplet).as("expanded values should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("pairPrefixFactoryCases")
		@DisplayName("of(pair, c) should expand pair into first and second")
		<A, B, C> void ofWithPairPrefixShouldExpandPair(final String as, final PairPrefixFactoryCase<A, B, C> tc)
		{
			var triplet = Triplet.of(tc.pair(), tc.third());

			assertThat(triplet).as("pair-prefix expansion should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("nestedPairFactoryCases")
		@DisplayName("of(nested pair) should expand into all three values")
		<A, B, C> void ofWithNestedPairShouldExpandValues(final String as, final NestedPairFactoryCase<A, B, C> tc)
		{
			var triplet = Triplet.of(tc.pair());

			assertThat(triplet).as("nested-pair expansion should match for %s", as).isEqualTo(tc.expected());
		}

		private static Stream<Arguments> directFactoryCases()
		{
			return Stream.of(
					DirectFactoryCase.shape("text number and boolean", "dawn", 21, true),
					DirectFactoryCase.shape("nullable first value", null, 0, false),
					DirectFactoryCase.shape("numeric textual decimal", 9, "ember", 4.5),
					DirectFactoryCase.shape("collection enum and long",
							java.util.List.of("a", "b"),
							Thread.State.RUNNABLE,
							99L)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> pairFactoryCases()
		{
			return Stream.of(
					PairFactoryCase.shape("pair with integer and boolean", "dawn", Pair.of(21, true),
							Triplet.of("dawn", 21, true)),
					PairFactoryCase.shape("pair with null second value", "ember", Pair.of(null, true),
							Triplet.of("ember", null, true)),
					PairFactoryCase.shape("pair with enum and decimal", 1, Pair.of(Thread.State.NEW, 3.14),
							Triplet.of(1, Thread.State.NEW, 3.14))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> pairPrefixFactoryCases()
		{
			return Stream.of(
					PairPrefixFactoryCase.shape("string-integer with boolean third",
							Pair.of("dawn", 21),
							true,
							Triplet.of("dawn", 21, true)),
					PairPrefixFactoryCase.shape("nullable second in pair",
							Pair.of("ember", null),
							"tail",
							Triplet.of("ember", null, "tail")),
					PairPrefixFactoryCase.shape("enum and list with decimal",
							Pair.of(Thread.State.BLOCKED, java.util.List.of("x", "y")),
							2.5,
							Triplet.of(Thread.State.BLOCKED, java.util.List.of("x", "y"), 2.5))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> nestedPairFactoryCases()
		{
			return Stream.of(
					NestedPairFactoryCase.shape("string with nested integer-boolean",
							Pair.of("dawn", Pair.of(21, true)),
							Triplet.of("dawn", 21, true)),
					NestedPairFactoryCase.shape("nullable inner first value",
							Pair.of("ember", Pair.of(null, 9L)),
							Triplet.of("ember", null, 9L)),
					NestedPairFactoryCase.shape("numeric with nested enum-string",
							Pair.of(5, Pair.of(Thread.State.NEW, "rune")),
							Triplet.of(5, Thread.State.NEW, "rune"))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record DirectFactoryCase<A, B, C>(String as, A first, B second, C third)
		{
			private static <A, B, C> DirectFactoryCase<A, B, C> shape(final String as, final A first, final B second,
			                                                          final C third)
			{
				return new DirectFactoryCase<>(as, first, second, third);
			}
		}

		private record PairFactoryCase<A, B, C>(String as, A first, Pair<B, C> pair, Triplet<A, B, C> expected)
		{
			private static <A, B, C> PairFactoryCase<A, B, C> shape(final String as, final A first,
			                                                        final Pair<B, C> pair,
			                                                        final Triplet<A, B, C> expected)
			{
				return new PairFactoryCase<>(as, first, pair, expected);
			}
		}

		private record PairPrefixFactoryCase<A, B, C>(String as, Pair<A, B> pair, C third, Triplet<A, B, C> expected)
		{
			private static <A, B, C> PairPrefixFactoryCase<A, B, C> shape(final String as, final Pair<A, B> pair,
			                                                              final C third,
			                                                              final Triplet<A, B, C> expected)
			{
				return new PairPrefixFactoryCase<>(as, pair, third, expected);
			}
		}

		private record NestedPairFactoryCase<A, B, C>(String as, Pair<A, Pair<B, C>> pair, Triplet<A, B, C> expected)
		{
			private static <A, B, C> NestedPairFactoryCase<A, B, C> shape(final String as,
			                                                              final Pair<A, Pair<B, C>> pair,
			                                                              final Triplet<A, B, C> expected)
			{
				return new NestedPairFactoryCase<>(as, pair, expected);
			}
		}
	}
}