package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade alchemize tests")
final class CascadeAlchemizeTest
{
	@Nested
	@DisplayName("Aspects of optional distillation")
	final class OptionalDistillationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("alchemizeCases")
		@DisplayName("should keep only present potion outcomes")
		void shouldKeepOnlyPresentPotionOutcomes(final String as, final AlchemizeCase tc)
		{
			var result = tc.source().alchemize(tc.potion());

			assertThat(result.summon())
					.as("alchemize should emit only present outcomes for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> alchemizeCases()
		{
			return Stream.of(
					new AlchemizeCase(
							"Only even runes transmute into words",
							Cascade.beckon(1, 2, 3, 4),
							n -> n % 2 == 0 ? Optional.of("even-" + n) : Optional.empty(),
							List.of("even-2", "even-4")
					),
					new AlchemizeCase(
							"All empty potions yield barren brook",
							Cascade.beckon(7, 8),
							_ -> Optional.empty(),
							List.of()
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record AlchemizeCase(String as, Cascade<Integer> source,
		                             Function<Integer, Optional<? extends String>> potion,
		                             List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of guards and abyss")
	final class GuardAndAbyssTests
	{
		@Test
		@DisplayName("should reject null potion for brook")
		void shouldRejectNullPotionForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).alchemize(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("potion may not be null");
		}

		@Test
		@DisplayName("should throw when potion returns null optional")
		void shouldThrowWhenPotionReturnsNullOptional()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).alchemize(_ -> null).summon())
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("should tolerate null potion for abyss")
		void shouldTolerateNullPotionForAbyss()
		{
			assertThat(Cascade.<Integer>abyss().alchemize(null))
					.as("nadir alchemize should remain nadir without guard checks")
					.isSameAs(Cascade.abyss());
		}
	}
}