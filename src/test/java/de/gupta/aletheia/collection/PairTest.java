package de.gupta.aletheia.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pair tests")
final class PairTest
{
	@Nested
	@DisplayName("Factory and aliases")
	final class FactoryAndAliasesTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("pairCases")
		@DisplayName("of() should preserve both values")
		void ofShouldPreserveBothValues(final String as, final PairCase tc)
		{
			var pair = Pair.of(tc.first(), tc.second());

			assertThat(pair.first()).as("first should match for %s", as).isEqualTo(tc.first());
			assertThat(pair.second()).as("second should match for %s", as).isEqualTo(tc.second());
			assertThat(pair.left()).as("left alias should match for %s", as).isEqualTo(tc.first());
			assertThat(pair.right()).as("right alias should match for %s", as).isEqualTo(tc.second());
		}

		private static Stream<Arguments> pairCases()
		{
			return Stream.of(
					PairCase.shape("numeric and text values", 7, "oracle"),
					PairCase.shape("nullable left value", null, "ember")
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record PairCase(String as, Integer first, String second)
		{
			private static PairCase shape(final String as, final Integer first, final String second)
			{
				return new PairCase(as, first, second);
			}
		}
	}

	@Nested
	@DisplayName("Transformations")
	final class TransformationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("transformFirstCases")
		@DisplayName("transformFirst() should update first value only")
		<R> void transformFirstShouldUpdateFirstValueOnly(final String as, final TransformFirstCase<R> tc)
		{
			var transformed = tc.source().transformFirst(tc.transformation());

			assertThat(transformed).as("first transformation should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("transformSecondCases")
		@DisplayName("transformSecond() should update second value only")
		<R> void transformSecondShouldUpdateSecondValueOnly(final String as, final TransformSecondCase<R> tc)
		{
			var transformed = tc.source().transformSecond(tc.transformation());

			assertThat(transformed).as("second transformation should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("nullTransformationCases")
		@DisplayName("transformation methods should reject null transformation")
		void transformationMethodsShouldRejectNullTransformation(final String as, final NullTransformationCase tc)
		{
			assertThatThrownBy(() -> tc.invocation().invoke())
					.as("null transformation should be rejected for %s", as)
					.isInstanceOf(NullPointerException.class);
		}

		private static Stream<Arguments> transformFirstCases()
		{
			return Stream.of(
					TransformFirstCase.shape("string to length", Pair.of("storm", 3), String::length, Pair.of(5, 3)),
					TransformFirstCase.shape("string to upper", Pair.of("storm", 3), String::toUpperCase,
							Pair.of("STORM", 3))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> transformSecondCases()
		{
			return Stream.of(
					TransformSecondCase.shape("multiply number", Pair.of("storm", 3), n -> n * 10,
							Pair.of("storm", 30)),
					TransformSecondCase.shape("render number", Pair.of("storm", 3), n -> "#" + n,
							Pair.of("storm", "#3"))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> nullTransformationCases()
		{
			return Stream.of(
					NullTransformationCase.shape("transformFirst with null",
							() -> Pair.of("storm", 3).transformFirst(null)),
					NullTransformationCase.shape("transformSecond with null",
							() -> Pair.of("storm", 3).transformSecond((Function<Integer, Integer>) null))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		@FunctionalInterface
		private interface ThrowingInvocation
		{
			void invoke();
		}

		private record TransformFirstCase<R>(String as, Pair<String, Integer> source,
		                                     Function<String, R> transformation, Pair<R, Integer> expected)
		{
			private static <R> TransformFirstCase<R> shape(final String as, final Pair<String, Integer> source,
			                                               final Function<String, R> transformation,
			                                               final Pair<R, Integer> expected)
			{
				return new TransformFirstCase<>(as, source, transformation, expected);
			}
		}

		private record TransformSecondCase<R>(String as, Pair<String, Integer> source,
		                                      Function<Integer, R> transformation, Pair<String, R> expected)
		{
			private static <R> TransformSecondCase<R> shape(final String as, final Pair<String, Integer> source,
			                                                final Function<Integer, R> transformation,
			                                                final Pair<String, R> expected)
			{
				return new TransformSecondCase<>(as, source, transformation, expected);
			}
		}

		private record NullTransformationCase(String as, ThrowingInvocation invocation)
		{
			private static NullTransformationCase shape(final String as, final ThrowingInvocation invocation)
			{
				return new NullTransformationCase(as, invocation);
			}
		}
	}
}