package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for Unfolding.adjudicate() static method")
final class UnfoldingAdjudicateTest
{
	@Nested
	@DisplayName("Tests for positive judgement cases")
	final class PositiveJudgementTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should return Myth when judgement is true")
		<T> void shouldReturnMythWhenJudgementIsTrue(String description, T apparition)
		{
			Unfolding<T> result = Unfolding.adjudicate(apparition, true);

			assertThat(result).as("adjudicate(%s, true) should return Myth instance", description)
							  .isInstanceOf(Myth.class)
							  .satisfies(unfolding ->
							  {
								  assertThat(unfolding.supple()).as("Result should be supple").isTrue();
								  assertThat(unfolding.sterile()).as("Result should not be sterile").isFalse();
							  });
		}

		@Test
		@DisplayName("should handle null apparition with true judgement")
		void shouldHandleNullApparitionWithTrueJudgement()
		{
			Unfolding<String> result = Unfolding.adjudicate(null, true);

			assertThat(result).as("adjudicate(null, true) should return Shell instance")
							  .isInstanceOf(Shell.class)
							  .satisfies(unfolding ->
							  {
								  assertThat(unfolding.sterile()).as("Result should be sterile").isTrue();
								  assertThat(unfolding.supple()).as("Result should not be supple").isFalse();
							  });
		}

		@Test
		@DisplayName("should preserve apparition value in result")
		void shouldPreserveApparitionValueInResult()
		{
			String value = "preserved";
			Unfolding<String> result = Unfolding.adjudicate(value, true);

			assertThat(result.summon()).as("adjudicate should preserve apparition value")
									   .isEqualTo(value);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("string value", "test"),
					new TestCase<>("integer value", 42),
					new TestCase<>("empty string", ""),
					new TestCase<>("boolean value", false),
					new TestCase<>("object value", new Object())
			).map(tc -> Arguments.of(tc.description, tc.apparition));
		}

		private record TestCase<T>(String description, T apparition)
		{
		}
	}

	@Nested
	@DisplayName("Tests for negative judgement cases")
	final class NegativeJudgementTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should return Shell when judgement is false")
		<T> void shouldReturnShellWhenJudgementIsFalse(String description, T apparition)
		{
			Unfolding<T> result = Unfolding.adjudicate(apparition, false);

			assertThat(result).as("adjudicate(%s, false) should return Shell instance", description)
							  .isInstanceOf(Shell.class)
							  .satisfies(unfolding ->
							  {
								  assertThat(unfolding.sterile()).as("Result should be sterile").isTrue();
								  assertThat(unfolding.supple()).as("Result should not be supple").isFalse();
							  });
		}

		@Test
		@DisplayName("should return Shell for null apparition with false judgement")
		void shouldReturnShellForNullApparitionWithFalseJudgement()
		{
			Unfolding<String> result = Unfolding.adjudicate(null, false);

			assertThat(result).as("adjudicate(null, false) should return Shell instance")
							  .isInstanceOf(Shell.class)
							  .satisfies(unfolding ->
							  {
								  assertThat(unfolding.sterile()).as("Result should be sterile").isTrue();
								  assertThat(unfolding.supple()).as("Result should not be supple").isFalse();
							  });
		}

		@Test
		@DisplayName("should ignore apparition value when judgement is false")
		void shouldIgnoreApparitionValueWhenJudgementIsFalse()
		{
			String value = "ignored";
			Unfolding<String> result = Unfolding.adjudicate(value, false);

			assertThat(result).as("adjudicate with false judgement should ignore apparition")
							  .isInstanceOf(Shell.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("string value", "test"),
					new TestCase<>("integer value", 42),
					new TestCase<>("empty string", ""),
					new TestCase<>("boolean value", true),
					new TestCase<>("object value", new Object())
			).map(tc -> Arguments.of(tc.description, tc.apparition));
		}

		private record TestCase<T>(String description, T apparition)
		{
		}
	}

	@Nested
	@DisplayName("Tests for edge cases and boundary conditions")
	final class EdgeCaseTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("edgeCaseTestData")
		@DisplayName("should handle edge cases correctly")
		<T> void shouldHandleEdgeCasesCorrectly(String description, T apparition, boolean judgement,
												Class<? extends Unfolding<T>> expectedType)
		{
			Unfolding<T> result = Unfolding.adjudicate(apparition, judgement);

			assertThat(result).as("adjudicate(%s, %s) should return %s", description, judgement,
									  expectedType.getSimpleName())
							  .isInstanceOf(expectedType);
		}

		@Test
		@DisplayName("should maintain consistency across multiple calls")
		void shouldMaintainConsistencyAcrossMultipleCalls()
		{
			String value = "consistent";

			Unfolding<String> result1 = Unfolding.adjudicate(value, true);
			Unfolding<String> result2 = Unfolding.adjudicate(value, true);

			assertThat(result1).as("Multiple calls should produce equivalent results")
							   .satisfies(r1 ->
							   {
								   assertThat(r1.supple()).isEqualTo(result2.supple());
								   assertThat(r1.sterile()).isEqualTo(result2.sterile());
								   assertThat(r1.summon()).isEqualTo(result2.summon());
							   });
		}

		@Test
		@DisplayName("should handle extreme values correctly")
		void shouldHandleExtremeValuesCorrectly()
		{
			Long maxValue = Long.MAX_VALUE;
			Long minValue = Long.MIN_VALUE;

			assertThat(Unfolding.adjudicate(maxValue, true)).as("Should handle MAX_VALUE")
															.isInstanceOf(Myth.class);
			assertThat(Unfolding.adjudicate(minValue, true)).as("Should handle MIN_VALUE")
															.isInstanceOf(Myth.class);
			assertThat(Unfolding.adjudicate(maxValue, false)).as("Should handle MAX_VALUE with false")
															 .isInstanceOf(Shell.class);
		}

		private static Stream<Arguments> edgeCaseTestData()
		{
			return Stream.of(
					Arguments.of("null with true", null, true, Shell.class),
					Arguments.of("null with false", null, false, Shell.class),
					Arguments.of("empty string with true", "", true, Myth.class),
					Arguments.of("empty string with false", "", false, Shell.class),
					Arguments.of("zero integer with true", 0, true, Myth.class),
					Arguments.of("zero integer with false", 0, false, Shell.class),
					Arguments.of("boolean false with true", false, true, Myth.class),
					Arguments.of("boolean true with false", true, false, Shell.class)
			);
		}
	}

	@Nested
	@DisplayName("Tests for functional behavior and composition")
	final class FunctionalBehaviorTests
	{
		@Test
		@DisplayName("should compose with other Unfolding operations")
		void shouldComposeWithOtherUnfoldingOperations()
		{
			String value = "compose";

			Unfolding<String> result = Unfolding.adjudicate(value, true)
												.metamorphose(String::toUpperCase);

			assertThat(result.summon()).as("Should compose with metamorphose")
									   .isEqualTo("COMPOSE");
		}

		@Test
		@DisplayName("should support fluent chaining with discern")
		void shouldSupportFluentChainingWithDiscern()
		{
			String value = "chain";

			Unfolding<String> result = Unfolding.adjudicate(value, true)
												.discern(s -> s.length() > 3);

			assertThat(result).as("Should support fluent chaining with discern")
							  .isInstanceOf(Myth.class);
		}

		@Test
		@DisplayName("should integrate with stream operations")
		void shouldIntegrateWithStreamOperations()
		{
			String value = "stream";

			Stream<String> stream = Unfolding.adjudicate(value, true).stream();

			assertThat(stream).as("Should integrate with stream operations")
							  .containsExactly(value);
		}

		@Test
		@DisplayName("should convert to optional correctly")
		void shouldConvertToOptionalCorrectly()
		{
			String value = "optional";

			assertThat(Unfolding.adjudicate(value, true).optional()).as("True judgement should create present Optional")
																	.isPresent()
																	.contains(value);

			assertThat(Unfolding.adjudicate(value, false).optional()).as("False judgement should create empty Optional")
																	 .isEmpty();
		}
	}

	@Nested
	@DisplayName("Tests for type safety and generics")
	final class TypeSafetyTests
	{
		@Test
		@DisplayName("should maintain type safety with different types")
		void shouldMaintainTypeSafetyWithDifferentTypes()
		{
			Integer intValue = 42;
			String stringValue = "type";

			Unfolding<Integer> intResult = Unfolding.adjudicate(intValue, true);
			Unfolding<String> stringResult = Unfolding.adjudicate(stringValue, true);

			assertThat(intResult.summon()).as("Should maintain Integer type")
										  .isInstanceOf(Integer.class)
										  .isEqualTo(42);

			assertThat(stringResult.summon()).as("Should maintain String type")
											 .isInstanceOf(String.class)
											 .isEqualTo("type");
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("typeTestCases")
		@DisplayName("should handle different generic types correctly")
		<T> void shouldHandleDifferentGenericTypesCorrectly(String description, T value, Class<T> type)
		{
			Unfolding<T> result = Unfolding.adjudicate(value, true);

			assertThat(result.summon()).as("Should handle %s correctly", description)
									   .isInstanceOf(type)
									   .isEqualTo(value);
		}

		private static Stream<Arguments> typeTestCases()
		{
			return Stream.of(
					new TypeTestCase<>("String", "test", String.class),
					new TypeTestCase<>("Integer", 123, Integer.class),
					new TypeTestCase<>("Double", 3.14, Double.class),
					new TypeTestCase<>("Boolean", true, Boolean.class),
					new TypeTestCase<>("Character", 'x', Character.class)
			).map(tc -> Arguments.of(tc.description, tc.value, tc.type));
		}

		private record TypeTestCase<T>(String description, T value, Class<T> type)
		{
		}
	}
}