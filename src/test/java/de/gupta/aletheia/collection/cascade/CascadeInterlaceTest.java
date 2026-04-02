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

@DisplayName("Cascade interlace tests")
final class CascadeInterlaceTest
{
	@Nested
	@DisplayName("Aspects of pairing with original current")
	final class PairingTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("interlaceCases")
		@DisplayName("should pair origin with interlaced value")
		void shouldPairOriginWithInterlacedValue(final String as, final InterlaceCase tc)
		{
			var result = tc.source().interlace(tc.interlacing());

			assertThat(result.summon())
					.as("interlace should weave expected pairs for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> interlaceCases()
		{
			return Stream.of(
					new InterlaceCase(
							"Names become name-length pairs",
							Cascade.beckon("odin", "thor"),
							String::length,
							List.of(Pair.of("odin", 4), Pair.of("thor", 4))
					),
					new InterlaceCase(
							"Interlacing may return null as second value",
							Cascade.beckon("a", "bb"),
							s -> s.length() == 1 ? null : s.length(),
							List.of(Pair.of("a", null), Pair.of("bb", 2))
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record InterlaceCase(String as, Cascade<String> source,
		                             Function<String, Integer> interlacing,
		                             List<Pair<String, Integer>> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of emptiness and null guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should stay nadir on abyss source")
		void shouldStayNadirOnAbyssSource()
		{
			assertThat(Cascade.<String>abyss().interlace(String::length)).isSameAs(Cascade.abyss());
		}

		@Test
		@DisplayName("should reject null interlacing on brook")
		void shouldRejectNullInterlacingOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("x").interlace((Function<String, Integer>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("interlacing may not be null");
		}
	}
}