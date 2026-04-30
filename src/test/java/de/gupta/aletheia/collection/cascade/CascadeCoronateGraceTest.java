package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade coronate(conclusion, grace) tests")
final class CascadeCoronateGraceTest
{
	@Nested
	@DisplayName("Aspects of brook coronation")
	final class BrookCoronationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("coronationCases")
		@DisplayName("should apply conclusion to brook and ignore grace")
		void shouldApplyConclusionToBrookAndIgnoreGrace(final String as, final CoronationCase tc)
		{
			var result = tc.source().coronate(tc.conclusion(), tc.grace());

			assertThat(result)
					.as("coronate should produce expected conclusion for %s", as)
					.isEqualTo(tc.expected());
		}

		private static Stream<Arguments> coronationCases()
		{
			return Stream.of(
					new CoronationCase(
							"River of names yields its count",
							Cascade.beckon("zeus", "hera", "ares"),
							Stream::count,
							() -> -1L,
							3L
					),
					new CoronationCase(
							"River is collected into a joined sentence",
							Cascade.beckon("the", "oracle", "speaks"),
							s -> s.map(Object::toString).collect(Collectors.joining(" ")),
							() -> "silence",
							"the oracle speaks"
					),
					new CoronationCase(
							"River yields its sinister survivor",
							Cascade.beckon("apollo", "athena"),
							s -> s.findFirst().orElse("none"),
							() -> "none",
							"apollo"
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record CoronationCase(String as,
		                              Cascade<Object> source,
		                              Function<Stream<Object>, Object> conclusion,
		                              java.util.function.Supplier<Object> grace,
		                              Object expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of grace not invoked on brook")
	final class GraceNotInvokedOnBrookTests
	{
		@Test
		@DisplayName("should never invoke grace when source is brook")
		void shouldNeverInvokeGraceWhenSourceIsBrook()
		{
			var called = new AtomicBoolean(false);

			var result = Cascade.beckon("atlas", "prometheus").coronate(
					Stream::count,
					() ->
					{
						called.set(true);
						return -1L;
					}
			);

			assertThat(called.get())
					.as("grace should not be invoked when brook is present")
					.isFalse();
			assertThat(result).isEqualTo(2L);
		}
	}

	@Nested
	@DisplayName("Aspects of nadir coronation")
	final class NadirCoronationTests
	{
		@Test
		@DisplayName("should invoke grace and return its value for abyss")
		void shouldInvokeGraceAndReturnItsValueForAbyss()
		{
			var result = Cascade.<String>abyss().coronate(
					s -> s.collect(Collectors.toList()),
					() -> List.of("revelation")
			);

			assertThat(result)
					.as("nadir coronate should return grace value")
					.isEqualTo(List.of("revelation"));
		}

		@Test
		@DisplayName("should return null from grace when grace supplies null")
		void shouldReturnNullFromGraceWhenGraceSuppliesNull()
		{
			var result = Cascade.<String>abyss().coronate(
					s -> s.findFirst().orElse(null),
					() -> null
			);

			assertThat(result)
					.as("nadir coronate should return null if grace supplies null")
					.isNull();
		}

		@Test
		@DisplayName("should never invoke conclusion for abyss")
		void shouldNeverInvokeConclusionForAbyss()
		{
			var called = new AtomicBoolean(false);

			Cascade.<String>abyss().coronate(
					s ->
					{
						called.set(true);
						return s.count();
					},
					() -> 0L
			);

			assertThat(called.get())
					.as("conclusion should not be invoked on nadir")
					.isFalse();
		}
	}

	@Nested
	@DisplayName("Aspects of guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should reject null grace for brook")
		void shouldRejectNullGraceForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("zeus").coronate(Stream::count, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("grace may not be null");
		}

		@Test
		@DisplayName("should reject null grace for abyss")
		void shouldRejectNullGraceForAbyss()
		{
			assertThatThrownBy(() -> Cascade.<String>abyss().coronate(Stream::count, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("grace may not be null");
		}
	}
}