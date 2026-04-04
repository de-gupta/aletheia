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
	}
}