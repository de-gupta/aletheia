package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade conjoin tests")
final class CascadeConjoinTest
{
	@Nested
	@DisplayName("Aspects of constant consort joining")
	final class ConstantConsortJoiningTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("conjoinCases")
		@DisplayName("should pair each element with same consort")
		void shouldPairEachElementWithSameConsort(final String as, final ConjoinCase tc)
		{
			var result = tc.source().conjoin(tc.consort(), tc.conjugation());

			assertThat(result.summon())
					.as("conjoin should apply same consort across current for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> conjoinCases()
		{
			return Stream.of(
					new ConjoinCase(
							"Each name is bound to rune length 3",
							Cascade.beckon("odin", "thor"),
							3,
							(name, n) -> name + "-" + n,
							List.of("odin-3", "thor-3")
					),
					new ConjoinCase(
							"Null outcomes from conjugation are filtered",
							Cascade.beckon("a", "bb", "ccc"),
							2,
							(name, n) -> name.length() == n ? null : name + n,
							List.of("a2", "ccc2")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record ConjoinCase(String as, Cascade<String> source,
		                           Integer consort,
		                           BiFunction<String, Integer, String> conjugation,
		                           List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of guards and abyss")
	final class GuardAndAbyssTests
	{
		@Test
		@DisplayName("should reject null consort for brook")
		void shouldRejectNullConsortForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("x").conjoin(null, (s, n) -> s + n))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("consort may not be null");
		}

		@Test
		@DisplayName("should reject null conjugation for brook")
		void shouldRejectNullConjugationForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("x").conjoin(1, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("conjugation may not be null");
		}

		@Test
		@DisplayName("should tolerate null args for abyss")
		void shouldTolerateNullArgsForAbyss()
		{
			assertThat(Cascade.<String>abyss().conjoin(null, null))
					.as("nadir conjoin should remain nadir without guard checks")
					.isSameAs(Cascade.abyss());
		}
	}
}