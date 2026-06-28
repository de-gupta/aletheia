package de.gupta.aletheia.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Triad tests")
final class TriadTest
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
			var triplet = Triad.of(tc.first(), tc.second(), tc.third());

			assertThat(triplet.dawn()).as("sinister should match for %s", as).isEqualTo(tc.first());
			assertThat(triplet.zenith()).as("dexter should match for %s", as).isEqualTo(tc.second());
			assertThat(triplet.dusk()).as("dusk should match for %s", as).isEqualTo(tc.third());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("pairFactoryCases")
		@DisplayName("of(a, dyad) should expand dyad into dexter and dusk")
		<A, B, C> void ofWithPairShouldExpandPair(final String as, final PairFactoryCase<A, B, C> tc)
		{
			var triplet = Triad.of(tc.first(), tc.dyad());

			assertThat(triplet).as("expanded values should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("pairPrefixFactoryCases")
		@DisplayName("of(dyad, c) should expand dyad into sinister and dexter")
		<A, B, C> void ofWithPairPrefixShouldExpandPair(final String as, final PairPrefixFactoryCase<A, B, C> tc)
		{
			var triplet = Triad.of(tc.dyad(), tc.third());

			assertThat(triplet).as("dyad-prefix expansion should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("nestedPairFactoryCases")
		@DisplayName("of(nested dyad) should expand into all three values")
		<A, B, C> void ofWithNestedPairShouldExpandValues(final String as, final NestedPairFactoryCase<A, B, C> tc)
		{
			var triplet = Triad.of(tc.dyad());

			assertThat(triplet).as("nested-dyad expansion should match for %s", as).isEqualTo(tc.expected());
		}

		private static Stream<Arguments> directFactoryCases()
		{
			return Stream.of(
					DirectFactoryCase.shape("text number and boolean", "dawn", 21, true),
					DirectFactoryCase.shape("nullable sinister value", null, 0, false),
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
					PairFactoryCase.shape("dyad with integer and boolean", "dawn", Dyad.of(21, true),
							Triad.of("dawn", 21, true)),
					PairFactoryCase.shape("dyad with null dexter value", "ember", Dyad.of(null, true),
							Triad.of("ember", null, true)),
					PairFactoryCase.shape("dyad with enum and decimal", 1, Dyad.of(Thread.State.NEW, 3.14),
							Triad.of(1, Thread.State.NEW, 3.14))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> pairPrefixFactoryCases()
		{
			return Stream.of(
					PairPrefixFactoryCase.shape("string-integer with boolean dusk",
							Dyad.of("dawn", 21),
							true,
							Triad.of("dawn", 21, true)),
					PairPrefixFactoryCase.shape("nullable dexter in dyad",
							Dyad.of("ember", null),
							"tail",
							Triad.of("ember", null, "tail")),
					PairPrefixFactoryCase.shape("enum and list with decimal",
							Dyad.of(Thread.State.BLOCKED, java.util.List.of("x", "y")),
							2.5,
							Triad.of(Thread.State.BLOCKED, java.util.List.of("x", "y"), 2.5))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> nestedPairFactoryCases()
		{
			return Stream.of(
					NestedPairFactoryCase.shape("string with nested integer-boolean",
							Dyad.of("dawn", Dyad.of(21, true)),
							Triad.of("dawn", 21, true)),
					NestedPairFactoryCase.shape("nullable inner sinister value",
							Dyad.of("ember", Dyad.of(null, 9L)),
							Triad.of("ember", null, 9L)),
					NestedPairFactoryCase.shape("numeric with nested enum-string",
							Dyad.of(5, Dyad.of(Thread.State.NEW, "rune")),
							Triad.of(5, Thread.State.NEW, "rune"))
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

		private record PairFactoryCase<A, B, C>(String as, A first, Dyad<B, C> dyad, Triad<A, B, C> expected)
		{
			private static <A, B, C> PairFactoryCase<A, B, C> shape(final String as, final A first,
			                                                        final Dyad<B, C> dyad,
			                                                        final Triad<A, B, C> expected)
			{
				return new PairFactoryCase<>(as, first, dyad, expected);
			}
		}

		private record PairPrefixFactoryCase<A, B, C>(String as, Dyad<A, B> dyad, C third, Triad<A, B, C> expected)
		{
			private static <A, B, C> PairPrefixFactoryCase<A, B, C> shape(final String as, final Dyad<A, B> dyad,
			                                                              final C third,
			                                                              final Triad<A, B, C> expected)
			{
				return new PairPrefixFactoryCase<>(as, dyad, third, expected);
			}
		}

		private record NestedPairFactoryCase<A, B, C>(String as, Dyad<A, Dyad<B, C>> dyad, Triad<A, B, C> expected)
		{
			private static <A, B, C> NestedPairFactoryCase<A, B, C> shape(final String as,
			                                                              final Dyad<A, Dyad<B, C>> dyad,
			                                                              final Triad<A, B, C> expected)
			{
				return new NestedPairFactoryCase<>(as, dyad, expected);
			}
		}
	}

	@org.junit.jupiter.api.Nested
	@DisplayName("Conventional accessor aliases")
	final class ConventionalAccessors
	{
		@org.junit.jupiter.api.Test
		@DisplayName("first() returns dawn")
		void firstReturnsDawn()
		{
			assertThat(Triad.of("a", "b", "c").first()).as("first() == dawn").isEqualTo("a");
		}

		@org.junit.jupiter.api.Test
		@DisplayName("second() returns zenith")
		void secondReturnsZenith()
		{
			assertThat(Triad.of("a", "b", "c").second()).as("second() == zenith").isEqualTo("b");
		}

		@org.junit.jupiter.api.Test
		@DisplayName("third() returns dusk")
		void thirdReturnsDusk()
		{
			assertThat(Triad.of("a", "b", "c").third()).as("third() == dusk").isEqualTo("c");
		}
	}
}