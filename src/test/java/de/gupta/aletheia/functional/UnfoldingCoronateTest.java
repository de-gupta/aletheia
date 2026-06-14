package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unfolding#coronate")
final class UnfoldingCoronateTest
{
	@Nested
	@DisplayName("coronate(Function, R) — maps present value or returns eager refuge")
	final class WithEagerRefuge
	{
		private record CoronateCase<T, R>(String as, Unfolding<T> source, Function<T, R> proclamation, R refuge,
		                                  R expected)
		{
			static <T, R> CoronateCase<T, R> of(final String as, final Unfolding<T> source,
			                                    final Function<T, R> proclamation, final R refuge, final R expected)
			{
				return new CoronateCase<>(as, source, proclamation, refuge, expected);
			}
		}

		@Nested
		@DisplayName("when value is present")
		final class WhenValueIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("appliesProclamationAndReturnsCases")
			@DisplayName("applies proclamation to value and returns result")
			<T, R> void appliesProclamationAndReturns(final String as, final CoronateCase<T, R> tc)
			{
				assertThat(tc.source().coronate(tc.proclamation(), tc.refuge()))
						.as(as)
						.isEqualTo(tc.expected());
			}

			private static Stream<Arguments> appliesProclamationAndReturnsCases()
			{
				return Stream.of(
						CoronateCase.of("String length", Unfolding.beckon("hello"),
								String::length, -1, 5),
						CoronateCase.of("Integer to String", Unfolding.beckon(42),
								n -> "value=" + n, "absent", "value=42"),
						CoronateCase.of("String uppercased", Unfolding.beckon("world"),
								String::toUpperCase, "absent", "WORLD")
				).map(tc -> Arguments.of(tc.as(), tc));
			}
		}

		@Nested
		@DisplayName("when Unfolding is empty")
		final class WhenUnfoldingIsEmpty
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("returnsRefugeWithoutApplyingProclamationCases")
			@DisplayName("returns refuge without applying proclamation")
			<R> void returnsRefugeWithoutApplyingProclamation(final String as, final R refuge, final R expected)
			{
				assertThat(Unfolding.<String>chaos().coronate(String::length, refuge))
						.as(as)
						.isEqualTo(expected);
			}

			@Test
			@DisplayName("does not invoke proclamation when source is empty")
			void doesNotInvokeProclamationWhenSourceIsEmpty()
			{
				final AtomicInteger proclamationCalls = new AtomicInteger();

				Unfolding.<String>chaos().coronate(
						s ->
						{
							proclamationCalls.incrementAndGet();
							return s.length();
						}, 0);

				assertThat(proclamationCalls.get())
						.as("proclamation call count on empty source")
						.isEqualTo(0);
			}

			private static Stream<Arguments> returnsRefugeWithoutApplyingProclamationCases()
			{
				return Stream.of(
						Arguments.of("Integer refuge", 0, 0),
						Arguments.of("String refuge", "fallback", "fallback"),
						Arguments.of("negative sentinel refuge", -1, -1)
				);
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when proclamation is null")
			void throwsWhenProclamationIsNull()
			{
				assertThatThrownBy(() -> Unfolding.beckon("x").coronate(null, 0))
						.as("null proclamation on present source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("proclamation may not be null");
			}

			@Test
			@DisplayName("throws when refuge is null")
			void throwsWhenRefugeIsNull()
			{
				assertThatThrownBy(() -> Unfolding.beckon("x").coronate(String::length, (Integer) null))
						.as("null refuge on present source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("refuge may not be null");
			}

			@Test
			@DisplayName("throws when refuge is null and source is empty")
			void throwsWhenRefugeIsNullAndSourceIsEmpty()
			{
				assertThatThrownBy(() -> Unfolding.<String>chaos().coronate(String::length, (Integer) null))
						.as("null refuge on empty source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("refuge may not be null");
			}
		}
	}

	@Nested
	@DisplayName("coronate(Function, Supplier<R>) — maps present value or evaluates lazy refuge")
	final class WithLazyRefuge
	{
		@Nested
		@DisplayName("when value is present")
		final class WhenValueIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("appliesProclamationWithoutEvaluatingRefugeCases")
			@DisplayName("applies proclamation and does not evaluate refuge supplier")
			<T, R> void appliesProclamationWithoutEvaluatingRefuge(final String as, final Unfolding<T> source,
			                                                       final Function<T, R> proclamation,
			                                                       final R expected)
			{
				final AtomicInteger refugeCalls = new AtomicInteger();

				final R result = source.coronate(proclamation, () ->
				{
					refugeCalls.incrementAndGet();
					return expected;
				});

				assertThat(result).as(as + " — result").isEqualTo(expected);
				assertThat(refugeCalls.get()).as(as + " — refuge supplier call count").isEqualTo(0);
			}

			private static Stream<Arguments> appliesProclamationWithoutEvaluatingRefugeCases()
			{
				return Stream.of(
						Arguments.of("String length", Unfolding.beckon("hello"),
								(Function<String, Integer>) String::length, 5),
						Arguments.of("Integer to String", Unfolding.beckon(42),
								(Function<Integer, String>) n -> "value=" + n, "value=42")
				);
			}
		}

		@Nested
		@DisplayName("when Unfolding is empty")
		final class WhenUnfoldingIsEmpty
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("evaluatesRefugeSupplierAndReturnsItsResultCases")
			@DisplayName("evaluates refuge supplier exactly once and returns its result")
			void evaluatesRefugeSupplierAndReturnsItsResult(final String as, final int refugeValue)
			{
				final AtomicInteger refugeCalls = new AtomicInteger();

				final int result = Unfolding.<String>chaos()
				                            .coronate(String::length, () ->
											{
												refugeCalls.incrementAndGet();
												return refugeValue;
											});

				assertThat(result).as(as + " — result").isEqualTo(refugeValue);
				assertThat(refugeCalls.get()).as(as + " — refuge supplier call count").isEqualTo(1);
			}

			private static Stream<Arguments> evaluatesRefugeSupplierAndReturnsItsResultCases()
			{
				return Stream.of(
						Arguments.of("positive refuge value", 99),
						Arguments.of("zero refuge value", 0),
						Arguments.of("negative sentinel refuge", -1)
				);
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when proclamation is null")
			void throwsWhenProclamationIsNull()
			{
				assertThatThrownBy(() -> Unfolding.beckon("x").coronate(null, () -> 0))
						.as("null proclamation on present source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("proclamation may not be null");
			}

			@Test
			@DisplayName("throws when refuge supplier is null")
			void throwsWhenRefugeSupplierIsNull()
			{
				assertThatThrownBy(() -> Unfolding.beckon("x").coronate(String::length, null))
						.as("null refuge supplier on present source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("refuge may not be null");
			}

			@Test
			@DisplayName("throws when refuge supplier is null and source is empty")
			void throwsWhenRefugeSupplierIsNullAndSourceIsEmpty()
			{
				assertThatThrownBy(() -> Unfolding.<String>chaos().coronate(String::length, null))
						.as("null refuge supplier on empty source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("refuge may not be null");
			}
		}
	}
}