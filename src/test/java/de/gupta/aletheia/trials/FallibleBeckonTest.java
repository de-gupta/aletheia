package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for Fallible.beckon() static method")
final class FallibleBeckonTest
{
	@Nested
	@DisplayName("Tests for triumph creation")
	final class TriumphCreationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("beckonCases")
		@DisplayName("should beckon a triumph for every boon")
		<T> void shouldBeckonATriumphForEveryBoon(final String description, final T boon)
		{
			Fallible<T> result = Fallible.beckon(boon);
			Object coronation = result.coronate(value -> value, _ -> null);

			assertThat(result).as("beckon(%s) should yield a triumph", description)
			                  .isInstanceOf(Triumph.class);
			assertThat(coronation)
					.as("beckon(%s) should preserve the boon", description)
					.isEqualTo(boon);
		}

		private static Stream<Arguments> beckonCases()
		{
			return Stream.of(
								 new BeckonCase<>("string boon", "Orpheus"),
								 new BeckonCase<>("integer boon", 7),
								 new BeckonCase<>("null boon", null))
			             .map(testCase -> Arguments.of(testCase.description, testCase.boon));
		}

		private record BeckonCase<T>(String description, T boon)
		{
		}
	}
}