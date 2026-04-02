package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade discern with wrath tests")
final class CascadeDiscernWrathTest
{
	@Nested
	@DisplayName("Aspects of judged passage")
	final class JudgedPassageTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("passageCases")
		@DisplayName("should keep elements when all satisfy judgement")
		void shouldKeepElementsWhenAllSatisfyJudgement(final String as, final WrathDiscernCase tc)
		{
			var result = tc.source().discern(tc.judgement(), tc.wrath());

			assertThat(result.summon())
					.as("discern(judgement, wrath) should preserve current for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> passageCases()
		{
			return Stream.of(
					new WrathDiscernCase(
							"When all warriors are even, none are smitten",
							Cascade.beckon(2, 4, 6),
							n -> n % 2 == 0,
							() -> new IllegalStateException("should never be called"),
							List.of(2, 4, 6)
					),
					new WrathDiscernCase(
							"Single worthy hero passes unchanged",
							Cascade.beckon(9),
							n -> n == 9,
							() -> new IllegalArgumentException("unused"),
							List.of(9)
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record WrathDiscernCase(String as, Cascade<Integer> source,
		                                Predicate<Integer> judgement,
		                                Supplier<? extends RuntimeException> wrath,
		                                List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of wrath invocation")
	final class WrathInvocationTests
	{
		@Test
		@DisplayName("should throw provided wrath when any element fails judgement")
		void shouldThrowProvidedWrathWhenAnyElementFailsJudgement()
		{
			assertThatThrownBy(() -> Cascade.beckon(2, 3, 4)
			                                .discern(n -> n % 2 == 0, () -> new IllegalStateException("fate denied"))
			                                .summon())
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("fate denied");
		}

		@Test
		@DisplayName("should reject null judgement for brook")
		void shouldRejectNullJudgementForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).discern(null, RuntimeException::new))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		@Test
		@DisplayName("should reject null wrath for brook")
		void shouldRejectNullWrathForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).discern(_ -> true, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("wrath may not be null");
		}

		@Test
		@DisplayName("should tolerate null arguments for abyss")
		void shouldTolerateNullArgumentsForAbyss()
		{
			assertThat(Cascade.<Integer>abyss().discern(null, null))
					.as("nadir discern with wrath should remain nadir without guard checks")
					.isSameAs(Cascade.abyss());
		}
	}
}