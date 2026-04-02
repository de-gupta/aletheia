package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade metamorphose pair tests")
final class CascadeMetamorphosePairTest
{
	@Nested
	@DisplayName("Aspects of fate and destiny")
	final class FateDestinyTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("pairCases")
		@DisplayName("should pair each element with both destinies")
		void shouldPairEachElementWithBothDestinies(final String as, final PairCase tc)
		{
			var result = tc.source().metamorphose(tc.fate(), tc.destiny());

			assertThat(result.summon())
					.as("dual metamorphose should forge expected pairs for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> pairCases()
		{
			return Stream.of(
					PairCase.shape(
							"Each name yields length and upper rune",
							Cascade.beckon("apollo", "odin"),
							String::length,
							String::toUpperCase,
							List.of(Pair.of(6, "APOLLO"), Pair.of(4, "ODIN"))
					),
					PairCase.shape(
							"Single name still receives both outcomes",
							Cascade.beckon("freya"),
							String::length,
							s -> s + "-blessed",
							List.of(Pair.of(5, "freya-blessed"))
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record PairCase(String as, Cascade<String> source,
		                        Function<String, Integer> fate,
		                        Function<String, String> destiny,
		                        List<Pair<Integer, String>> expected)
		{
			private static PairCase shape(
					final String as,
					final Cascade<String> source,
					final Function<String, Integer> fate,
					final Function<String, String> destiny,
					final List<Pair<Integer, String>> expected)
			{
				return new PairCase(as, source, fate, destiny, expected);
			}
		}
	}

	@Nested
	@DisplayName("Aspects of empty source")
	final class EmptySourceTests
	{
		@Test
		@DisplayName("should remain nadir and skip both functions")
		void shouldRemainNadirAndSkipBothFunctions()
		{
			var result = Cascade.<String>abyss().metamorphose(
					_ ->
					{
						throw new AssertionError("fate should not be invoked");
					},
					_ ->
					{
						throw new AssertionError("destiny should not be invoked");
					}
			);

			assertThat(result).isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of null functions")
	final class NullFunctionTests
	{
		@Test
		@DisplayName("should reject null fate")
		void shouldRejectNullFate()
		{
			assertThatThrownBy(() -> Cascade.beckon("thor").metamorphose(null, Function.identity()))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("fate may not be null");
		}

		@Test
		@DisplayName("should reject null destiny")
		void shouldRejectNullDestiny()
		{
			assertThatThrownBy(() -> Cascade.beckon("thor").metamorphose(Function.identity(), null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("destiny may not be null");
		}
	}
}