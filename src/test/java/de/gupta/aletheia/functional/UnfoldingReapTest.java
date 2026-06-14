package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unfolding#reap")
final class UnfoldingReapTest
{
	@Nested
	@DisplayName("reap(R) — eager constant terminal")
	final class EagerHarvest
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("returnsHarvestRegardlessOfSourceStateCases")
		@DisplayName("returns harvest regardless of source state")
		<T, R> void returnsHarvestRegardlessOfSourceState(final String as, final HarvestCase<T, R> tc)
		{
			assertThat(tc.source().reap(tc.harvest()))
					.as(as)
					.isEqualTo(tc.harvest());
		}

		@Test
		@DisplayName("returns harvest when prior chain step produces empty without throwing")
		void returnsHarvestWhenPriorChainStepProducesEmpty()
		{
			final String result = Unfolding.beckon("hi")
			                               .discern(s -> s.length() > 10)
			                               .reap("constant");

			assertThat(result).as("chain became empty — reap should still return harvest").isEqualTo("constant");
		}

		@Test
		@DisplayName("returns harvest when prior chain step stays present")
		void returnsHarvestWhenPriorChainStepStaysPresent()
		{
			final String result = Unfolding.beckon("hello world")
			                               .discern(s -> s.length() > 5)
			                               .reap("constant");

			assertThat(result).as("chain stayed present — reap should still return harvest").isEqualTo("constant");
		}

		@Test
		@DisplayName("allows chain exceptions to propagate before reaping")
		void allowsChainExceptionsToPropagateBeforeReaping()
		{
			assertThatThrownBy(() ->
					Unfolding.beckon("hello")
					         .discern(s -> s.length() > 10, IllegalArgumentException::new)
					         .reap("unreachable"))
					.as("exception from prior discern should propagate")
					.isInstanceOf(IllegalArgumentException.class);
		}

		private static Stream<Arguments> returnsHarvestRegardlessOfSourceStateCases()
		{
			return Stream.of(
					HarvestCase.of("present Integer source — String harvest",
							Unfolding.beckon(42), "constant"),
					HarvestCase.of("empty source — String harvest",
							Unfolding.<Integer>chaos(), "constant"),
					HarvestCase.of("present String source — Integer harvest",
							Unfolding.beckon("hello"), 99),
					HarvestCase.of("empty source — Integer harvest",
							Unfolding.<String>chaos(), 99)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record HarvestCase<T, R>(String as, Unfolding<T> source, R harvest)
		{
			static <T, R> HarvestCase<T, R> of(final String as, final Unfolding<T> source, final R harvest)
			{
				return new HarvestCase<>(as, source, harvest);
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when harvest is null")
			void throwsWhenHarvestIsNull()
			{
				assertThatThrownBy(() -> Unfolding.beckon(1).reap((String) null))
						.as("null harvest on present source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("harvest may not be null");
			}

			@Test
			@DisplayName("throws when harvest is null and source is empty")
			void throwsWhenHarvestIsNullAndSourceIsEmpty()
			{
				assertThatThrownBy(() -> Unfolding.<Integer>chaos().reap((String) null))
						.as("null harvest on empty source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("harvest may not be null");
			}
		}
	}

	@Nested
	@DisplayName("reap(Supplier<R>) — lazy constant terminal")
	final class LazyHarvest
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("returnsSupplierResultRegardlessOfSourceStateCases")
		@DisplayName("returns supplier result regardless of source state")
		<T> void returnsSupplierResultRegardlessOfSourceState(final String as, final Unfolding<T> source,
		                                                      final String expected)
		{
			assertThat(source.reap(() -> expected))
					.as(as)
					.isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("evaluatesSupplierExactlyOnceCases")
		@DisplayName("evaluates supplier exactly once regardless of source state")
		<T> void evaluatesSupplierExactlyOnce(final String as, final Unfolding<T> source)
		{
			final AtomicInteger calls = new AtomicInteger();

			source.reap(() ->
			{
				calls.incrementAndGet();
				return "x";
			});

			assertThat(calls.get()).as(as).isEqualTo(1);
		}

		@Test
		@DisplayName("returns supplier result when prior chain step produces empty without throwing")
		void returnsSupplierResultWhenPriorChainStepProducesEmpty()
		{
			final String result = Unfolding.beckon("hi")
			                               .discern(s -> s.length() > 10)
			                               .reap(() -> "constant");

			assertThat(result).as("chain became empty — reap supplier should still be evaluated").isEqualTo("constant");
		}

		@Test
		@DisplayName("returns supplier result when prior chain step stays present")
		void returnsSupplierResultWhenPriorChainStepStaysPresent()
		{
			final String result = Unfolding.beckon("hello world")
			                               .discern(s -> s.length() > 5)
			                               .reap(() -> "constant");

			assertThat(result).as("chain stayed present — reap supplier should still be evaluated")
			                  .isEqualTo("constant");
		}

		@Test
		@DisplayName("allows chain exceptions to propagate before reaping")
		void allowsChainExceptionsToPropagateBeforeReaping()
		{
			assertThatThrownBy(() ->
					Unfolding.beckon("hello")
					         .discern(s -> s.length() > 10, IllegalArgumentException::new)
					         .reap(() -> "unreachable"))
					.as("exception from prior discern should propagate")
					.isInstanceOf(IllegalArgumentException.class);
		}

		private static Stream<Arguments> returnsSupplierResultRegardlessOfSourceStateCases()
		{
			return Stream.of(
					Arguments.of("present Integer source — String harvest", Unfolding.beckon(42), "harvest"),
					Arguments.of("empty source — String harvest", Unfolding.<Integer>chaos(), "harvest"),
					Arguments.of("present String source — Integer harvest", Unfolding.beckon("hello"), "result"),
					Arguments.of("empty String source — Integer harvest", Unfolding.<String>chaos(), "result")
			);
		}

		private static Stream<Arguments> evaluatesSupplierExactlyOnceCases()
		{
			return Stream.of(
					Arguments.of("present source supplier call count", Unfolding.beckon(42)),
					Arguments.of("empty source supplier call count", Unfolding.<Integer>chaos())
			);
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when harvest supplier is null")
			void throwsWhenHarvestSupplierIsNull()
			{
				assertThatThrownBy(() -> Unfolding.beckon(1).reap((Supplier<String>) null))
						.as("null supplier on present source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("harvest may not be null");
			}

			@Test
			@DisplayName("throws when harvest supplier is null and source is empty")
			void throwsWhenHarvestSupplierIsNullAndSourceIsEmpty()
			{
				assertThatThrownBy(() -> Unfolding.<Integer>chaos().reap((Supplier<String>) null))
						.as("null supplier on empty source")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("harvest may not be null");
			}
		}
	}
}