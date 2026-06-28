package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingVerdictTest
{
	// ── Shared test data ─────────────────────────────────────────────────────

	private static final Predicate<Integer> IS_NEGATIVE = n -> n < 0;
	private static final Predicate<Integer> IS_ZERO = n -> n == 0;
	private static final Predicate<Integer> IS_POSITIVE = n -> n > 0;

	@Nested
	@DisplayName("Branch routing — when(Predicate, Function)")
	class WhenFunctionRoutingTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should route to the first matching branch")
		@MethodSource("routingCases")
		void testRouting(final String description, final Unfolding<Integer> source, final String expected)
		{
			final String result = source.verdict()
			                            .when(IS_NEGATIVE, n -> "negative: " + n)
			                            .when(IS_ZERO, _ -> "zero")
			                            .when(IS_POSITIVE, n -> "positive: " + n)
			                            .fulminate();

			assertThat(result).as(description).isEqualTo(expected);
		}

		@Test
		@DisplayName("should pass the original value into the matching branch function")
		void testValuePassedToBranch()
		{
			final String result = Unfolding.beckon(42)
			                               .verdict()
			                               .when(IS_POSITIVE, n -> "value=" + n)
			                               .fulminate();

			assertThat(result).isEqualTo("value=42");
		}

		@Test
		@DisplayName("should stop at the first matching predicate — later matches ignored")
		void testFirstMatchWins()
		{
			final String result = Unfolding.beckon(5)
			                               .verdict()
			                               .when(n -> n > 0, _ -> "positive")
			                               .when(n -> n > 3, _ -> "greater than three")
			                               .fulminate();

			assertThat(result).isEqualTo("positive");
		}

		private static Stream<Arguments> routingCases()
		{
			return Stream.of(
					Arguments.of("negative routes to first branch", Unfolding.beckon(-3), "negative: -3"),
					Arguments.of("zero routes to second branch", Unfolding.beckon(0), "zero"),
					Arguments.of("positive routes to third branch", Unfolding.beckon(7), "positive: 7")
			);
		}
	}

	@Nested
	@DisplayName("Branch routing — when(Predicate, Supplier)")
	class WhenSupplierRoutingTests
	{
		@Test
		@DisplayName("should evaluate only the matching supplier")
		void testOnlyMatchingSupplierEvaluated()
		{
			final AtomicInteger negativeCalls = new AtomicInteger();
			final AtomicInteger zeroCalls = new AtomicInteger();
			final AtomicInteger positiveCalls = new AtomicInteger();

			Unfolding.beckon(7)
			         .verdict()
			         .when(IS_NEGATIVE, () ->
					 {
						 negativeCalls.incrementAndGet();
						 return "negative";
					 })
			         .when(IS_ZERO, () ->
					 {
						 zeroCalls.incrementAndGet();
						 return "zero";
					 })
			         .when(IS_POSITIVE, () ->
					 {
						 positiveCalls.incrementAndGet();
						 return "positive";
					 })
			         .fulminate();

			assertThat(negativeCalls.get()).isEqualTo(0);
			assertThat(zeroCalls.get()).isEqualTo(0);
			assertThat(positiveCalls.get()).isEqualTo(1);
		}

		@ParameterizedTest(name = "{0}")
		@DisplayName("should route to the correct branch")
		@MethodSource("supplierRoutingCases")
		void testRouting(final String description, final Unfolding<Integer> source, final String expected)
		{
			final String result = source.verdict()
			                            .when(IS_NEGATIVE, () -> "negative")
			                            .when(IS_ZERO, () -> "zero")
			                            .when(IS_POSITIVE, () -> "positive")
			                            .fulminate();

			assertThat(result).as(description).isEqualTo(expected);
		}

		private static Stream<Arguments> supplierRoutingCases()
		{
			return Stream.of(
					Arguments.of("negative value", Unfolding.beckon(-1), "negative"),
					Arguments.of("zero value", Unfolding.beckon(0), "zero"),
					Arguments.of("positive value", Unfolding.beckon(4), "positive")
			);
		}
	}

	@Nested
	@DisplayName("Branch routing — when(Predicate, R)")
	class WhenRawValueRoutingTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should route to the correct raw value")
		@MethodSource("rawRoutingCases")
		void testRouting(final String description, final Unfolding<Integer> source, final String expected)
		{
			final String result = source.verdict()
			                            .when(IS_NEGATIVE, "negative")
			                            .when(IS_ZERO, "zero")
			                            .when(IS_POSITIVE, "positive")
			                            .fulminate();

			assertThat(result).as(description).isEqualTo(expected);
		}

		private static Stream<Arguments> rawRoutingCases()
		{
			return Stream.of(
					Arguments.of("negative value", Unfolding.beckon(-5), "negative"),
					Arguments.of("zero value", Unfolding.beckon(0), "zero"),
					Arguments.of("positive value", Unfolding.beckon(9), "positive")
			);
		}
	}

	@Nested
	@DisplayName("Terminal — infuse(R) and infuse(Supplier)")
	class InfuseTerminalTests
	{
		@Test
		@DisplayName("infuse(R) returns matched value when a predicate matches")
		void testInfuseRWithMatch()
		{
			final String result = Unfolding.beckon(5)
			                               .verdict()
			                               .when(IS_POSITIVE, _ -> "positive")
			                               .infuse("default");

			assertThat(result).isEqualTo("positive");
		}

		@Test
		@DisplayName("infuse(R) returns default when no predicate matches")
		void testInfuseRNoMatch()
		{
			final String result = Unfolding.beckon(0)
			                               .verdict()
			                               .when(IS_NEGATIVE, _ -> "negative")
			                               .when(IS_POSITIVE, _ -> "positive")
			                               .infuse("default");

			assertThat(result).isEqualTo("default");
		}

		@Test
		@DisplayName("infuse(Supplier) evaluates supplier lazily only on no match")
		void testInfuseSupplierLaziness()
		{
			final AtomicInteger supplierCalls = new AtomicInteger();

			Unfolding.beckon(5)
			         .verdict()
			         .when(IS_POSITIVE, _ -> "positive")
			         .infuse(() ->
					 {
						 supplierCalls.incrementAndGet();
						 return "default";
					 });

			assertThat(supplierCalls.get()).isEqualTo(0);
		}

		@Test
		@DisplayName("infuse(Supplier) evaluates supplier when no match")
		void testInfuseSupplierCalledOnNoMatch()
		{
			final AtomicInteger supplierCalls = new AtomicInteger();

			final String result = Unfolding.beckon(0)
			                               .verdict()
			                               .when(IS_NEGATIVE, _ -> "negative")
			                               .infuse(() ->
										   {
											   supplierCalls.incrementAndGet();
											   return "default";
										   });

			assertThat(result).isEqualTo("default");
			assertThat(supplierCalls.get()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("Terminal — smite(Supplier)")
	class SmiteTerminalTests
	{
		@Test
		@DisplayName("should return matched value when a predicate matches")
		void testSmiteWithMatch()
		{
			final String result = Unfolding.beckon(5)
			                               .verdict()
			                               .when(IS_POSITIVE, _ -> "positive")
			                               .smite(() -> new IllegalStateException("no match"));

			assertThat(result).isEqualTo("positive");
		}

		@Test
		@DisplayName("should throw caller's exception when no predicate matches")
		void testSmiteThrowsOnNoMatch()
		{
			assertThatThrownBy(() -> Unfolding.beckon(0)
			                                  .verdict()
			                                  .when(IS_NEGATIVE, _ -> "negative")
			                                  .when(IS_POSITIVE, _ -> "positive")
			                                  .smite(() -> new IllegalArgumentException("unmatched")))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("unmatched");
		}
	}

	@Nested
	@DisplayName("Terminal — fulminate()")
	class FulminateTerminalTests
	{
		@Test
		@DisplayName("should return matched value when a predicate matches")
		void testFulminateWithMatch()
		{
			final String result = Unfolding.beckon(-3)
			                               .verdict()
			                               .when(IS_NEGATIVE, _ -> "negative")
			                               .when(IS_ZERO, _ -> "zero")
			                               .when(IS_POSITIVE, _ -> "positive")
			                               .fulminate();

			assertThat(result).isEqualTo("negative");
		}

		@Test
		@DisplayName("should throw EmptyUnfoldingException with exhaustiveness message when no predicate matches")
		void testFulminateThrowsOnNoMatch()
		{
			assertThatThrownBy(() -> Unfolding.beckon(0)
			                                  .verdict()
			                                  .when(IS_NEGATIVE, _ -> "negative")
			                                  .when(IS_POSITIVE, _ -> "positive")
			                                  .fulminate())
					.isInstanceOf(EmptyUnfoldingException.class)
					.hasMessageContaining("exhaustive");
		}
	}

	@Nested
	@DisplayName("Terminal — pronounce() → Unfolding<R>")
	class PronounceTerminalTests
	{
		@Test
		@DisplayName("should return Unfolding containing matched value")
		void testPronounceWithMatch()
		{
			final Unfolding<String> result = Unfolding.beckon(5)
			                                          .verdict()
			                                          .when(IS_POSITIVE, _ -> "positive")
			                                          .pronounce();

			assertThat(result).isEqualTo(Unfolding.beckon("positive"));
		}

		@Test
		@DisplayName("should return chaos when no predicate matches")
		void testPronounceNoMatch()
		{
			final Unfolding<String> result = Unfolding.beckon(0)
			                                          .verdict()
			                                          .when(IS_NEGATIVE, _ -> "negative")
			                                          .when(IS_POSITIVE, _ -> "positive")
			                                          .pronounce();

			assertThat(result).isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("pronounce() result is chainable as Unfolding")
		void testPronounceIsChainable()
		{
			final String result = Unfolding.beckon(7)
			                               .verdict()
			                               .when(IS_POSITIVE, n -> n * 2)
			                               .pronounce()
			                               .metamorphose(n -> "doubled: " + n)
			                               .summon();

			assertThat(result).isEqualTo("doubled: 14");
		}
	}

	@Nested
	@DisplayName("Empty source behavior")
	class EmptySourceTests
	{
		@Test
		@DisplayName("infuse(R) throws EmptyUnfoldingException")
		void testInfuseThrows()
		{
			assertThatThrownBy(() -> Unfolding.<Integer>chaos()
			                                  .verdict()
			                                  .when(IS_POSITIVE, _ -> "positive")
			                                  .infuse("default"))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("infuse(Supplier) throws EmptyUnfoldingException")
		void testInfuseSupplierThrows()
		{
			assertThatThrownBy(() -> Unfolding.<Integer>chaos()
			                                  .verdict()
			                                  .when(IS_POSITIVE, _ -> "positive")
			                                  .infuse(() -> "default"))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("smite() throws EmptyUnfoldingException")
		void testSmiteThrows()
		{
			assertThatThrownBy(() -> Unfolding.<Integer>chaos()
			                                  .verdict()
			                                  .when(IS_POSITIVE, _ -> "positive")
			                                  .smite(IllegalStateException::new))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("fulminate() throws EmptyUnfoldingException")
		void testFulminateThrows()
		{
			assertThatThrownBy(() -> Unfolding.<Integer>chaos()
			                                  .verdict()
			                                  .when(IS_POSITIVE, _ -> "positive")
			                                  .fulminate())
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("pronounce() returns chaos")
		void testPronounceReturnsChaos()
		{
			final Unfolding<String> result = Unfolding.<Integer>chaos()
			                                          .verdict()
			                                          .when(IS_POSITIVE, _ -> "positive")
			                                          .pronounce();

			assertThat(result).isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("when() calls on empty source are ignored — no NPE on null args")
		void testWhenCallsIgnoredOnEmpty()
		{
			assertThat(Unfolding.<Integer>chaos()
			                    .verdict()
			                    .when(IS_POSITIVE, _ -> "positive")
			                    .when(IS_NEGATIVE, () -> "negative")
			                    .when(IS_ZERO, "zero")
			                    .pronounce())
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("when(pred, Supplier) as first call on empty source returns empty from pronounce()")
		void whenSupplierOnEmptyIsIgnored()
		{
			assertThat(Unfolding.<Integer>chaos()
			                    .verdict()
			                    .when(IS_POSITIVE, () -> "from supplier")
			                    .pronounce())
					.as("EmptyVerdict.when(pred, Supplier) contributes nothing — pronounce returns chaos")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("when(pred, R) as first call on empty source returns empty from pronounce()")
		void whenRawValueOnEmptyIsIgnored()
		{
			assertThat(Unfolding.<Integer>chaos()
			                    .verdict()
			                    .when(IS_POSITIVE, "eager value")
			                    .pronounce())
					.as("EmptyVerdict.when(pred, R) contributes nothing — pronounce returns chaos")
					.isEqualTo(Unfolding.chaos());
		}
	}

	@Nested
	@DisplayName("Null validation")
	class NullValidationTests
	{
		@Test
		@DisplayName("when(null, Function) throws NullPointerException")
		void testNullJudgementInWhenFunction()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(null, _ -> "x"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@Test
		@DisplayName("when(pred, null Function) throws NullPointerException")
		void testNullRewardInWhenFunction()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(IS_POSITIVE, (Function<Integer, String>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reward may not be null");
		}

		@Test
		@DisplayName("when(pred, null Supplier) throws NullPointerException")
		void testNullRewardInWhenSupplier()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(IS_POSITIVE, (Supplier<String>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reward may not be null");
		}

		@Test
		@DisplayName("when(pred, null R) throws NullPointerException")
		void testNullRewardInWhenRaw()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(IS_POSITIVE, (String) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reward may not be null");
		}

		@Test
		@DisplayName("infuse(null R) throws NullPointerException")
		void testNullInfuseR()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(IS_NEGATIVE, _ -> "x")
			                                  .infuse((String) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("manifestation may not be null");
		}

		@Test
		@DisplayName("infuse(null Supplier) throws NullPointerException")
		void testNullInfuseSupplier()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(IS_NEGATIVE, _ -> "x")
			                                  .infuse((Supplier<String>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("revelation may not be null");
		}

		@Test
		@DisplayName("smite(null) throws NullPointerException")
		void testNullSmiteWrath()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1)
			                                  .verdict()
			                                  .when(IS_NEGATIVE, _ -> "x")
			                                  .smite(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("wrath may not be null");
		}
	}
}