package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade weave tests")
final class CascadeWeaveTest
{
	@Nested
	@DisplayName("Aspects of reduction flow")
	final class ReductionFlowTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("weaveCases")
		@DisplayName("should fold current into initial state")
		void shouldFoldCurrentIntoInitialState(final String as, final WeaveCase tc)
		{
			var result = tc.source().weave("seed", tc.operation());

			assertThat(result)
					.as("weave should reduce as expected for %s", as)
					.isEqualTo(tc.expected());
		}

		private static Stream<Arguments> weaveCases()
		{
			return Stream.of(
					new WeaveCase(
							"Brook appends each rune in procession",
							Cascade.beckon(1, 2, 3),
							(acc, n) -> acc + "-" + n,
							"seed-1-2-3"
					),
					new WeaveCase(
							"Abyss leaves the seed untouched",
							Cascade.abyss(),
							(acc, n) -> acc + "-" + n,
							"seed"
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record WeaveCase(String as, Cascade<Integer> source,
		                         BiFunction<String, Integer, String> operation,
		                         String expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of operation guards")
	final class OperationGuardTests
	{
		@Test
		@DisplayName("should reject null operation for brook")
		void shouldRejectNullOperationForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).weave("seed", null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("operation may not be null");
		}

		@Test
		@DisplayName("should reject null operation for abyss")
		void shouldRejectNullOperationForAbyss()
		{
			assertThatThrownBy(() -> Cascade.<Integer>abyss().weave("seed", null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("operation may not be null");
		}
	}
}