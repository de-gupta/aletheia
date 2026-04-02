package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.functional.Unfolding;
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

@DisplayName("Cascade braid tests")
final class CascadeBraidTest
{
	@Nested
	@DisplayName("Aspects of brook weaving")
	final class BrookWeavingTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("braidCases")
		@DisplayName("should weave both cascades index-wise up to max size")
		void shouldWeaveBothCascadesIndexWiseUpToMaxSize(final String as, final BraidCase tc)
		{
			var result = tc.source().braid(tc.consort(), tc.weaver());

			assertThat(result.summon())
					.as("braid should produce woven omens for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> braidCases()
		{
			return Stream.of(
					new BraidCase(
							"Equal rivers weave paired runes",
							Cascade.beckon(1, 2),
							Cascade.beckon("A", "B"),
							(l, r) -> l.rescue(-1) + ":" + r.rescue("?"),
							List.of("1:A", "2:B")
					),
					new BraidCase(
							"Longer left river invokes chaos on right tail",
							Cascade.beckon(7, 8, 9),
							Cascade.beckon("X"),
							(l, r) -> l.rescue(-1) + "|" + r.rescue("void"),
							List.of("7|X", "8|void", "9|void")
					),
					new BraidCase(
							"Longer right river invokes chaos on left tail",
							Cascade.beckon(4),
							Cascade.beckon("M", "N"),
							(l, r) -> l.rescue(-1) + "-" + r.rescue("void"),
							List.of("4-M", "-1-N")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record BraidCase(String as, Cascade<Integer> source,
		                         Cascade<String> consort,
		                         BiFunction<Unfolding<Integer>, Unfolding<String>, String> weaver,
		                         List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of nadir weaving")
	final class NadirWeavingTests
	{
		@Test
		@DisplayName("should weave from consort only when source is abyss")
		void shouldWeaveFromConsortOnlyWhenSourceIsAbyss()
		{
			var result = Cascade.<Integer>abyss().braid(
					Cascade.beckon("alpha", "beta"),
					(left, right) -> left.rescue(-1) + ":" + right.rescue("?")
			);

			assertThat(result.summon()).containsExactly("-1:alpha", "-1:beta");
		}

		@Test
		@DisplayName("should yield empty brook when both are abyss")
		void shouldYieldEmptyBrookWhenBothAreAbyss()
		{
			var result = Cascade.<Integer>abyss().braid(
					Cascade.abyss(),
					(_, _) -> "unused"
			);

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.summon()).isEmpty();
		}
	}

	@Nested
	@DisplayName("Aspects of guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should reject null consort")
		void shouldRejectNullConsort()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).braid(null, (_, _) -> "x"))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("consort may not be null");
		}

		@Test
		@DisplayName("should reject null weaver")
		void shouldRejectNullWeaver()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).braid(Cascade.beckon("x"), null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("weaver may not be null");
		}
	}
}