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

@DisplayName("Dyad tests")
final class DyadTest
{
	@Nested
	@DisplayName("Factory and aliases")
	final class FactoryAndAliasesTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("pairCases")
		@DisplayName("of() should preserve both values")
		<A, B> void ofShouldPreserveBothValues(final String as, final PairCase<A, B> tc)
		{
			var pair = Dyad.of(tc.first(), tc.second());

			assertThat(pair.sinister()).as("sinister should match for %s", as).isEqualTo(tc.first());
			assertThat(pair.dexter()).as("dexter should match for %s", as).isEqualTo(tc.second());
		}

		private static Stream<Arguments> pairCases()
		{
			return Stream.of(
					PairCase.shape("numeric and text values", 7, "oracle"),
					PairCase.shape("nullable left value", null, "ember"),
					PairCase.shape("enum and list values", Thread.State.WAITING, java.util.List.of("north", "south"))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record PairCase<A, B>(String as, A first, B second)
		{
			private static <A, B> PairCase<A, B> shape(final String as, final A first, final B second)
			{
				return new PairCase<>(as, first, second);
			}
		}
	}

	@Nested
	@DisplayName("Transformations")
	final class TransformationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("transformFirstCases")
		@DisplayName("transformFirst() should update sinister value only")
		<R> void transformFirstShouldUpdateFirstValueOnly(final String as, final TransformFirstCase<R> tc)
		{
			var transformed = tc.source().transformSinister(tc.transformation());

			assertThat(transformed).as("sinister transformation should match for %s", as).isEqualTo(tc.expected());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("transformSecondCases")
		@DisplayName("transformSecond() should update dexter value only")
		<R> void transformSecondShouldUpdateSecondValueOnly(final String as, final TransformSecondCase<R> tc)
		{
			var transformed = tc.source().transformDexter(tc.transformation());

			assertThat(transformed).as("dexter transformation should match for %s", as).isEqualTo(tc.expected());
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
					TransformFirstCase.shape("string to length", Dyad.of("storm", 3), String::length, Dyad.of(5, 3)),
					TransformFirstCase.shape("string to upper", Dyad.of("storm", 3), String::toUpperCase,
							Dyad.of("STORM", 3))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> transformSecondCases()
		{
			return Stream.of(
					TransformSecondCase.shape("multiply number", Dyad.of("storm", 3), n -> n * 10,
							Dyad.of("storm", 30)),
					TransformSecondCase.shape("render number", Dyad.of("storm", 3), n -> "#" + n,
							Dyad.of("storm", "#3"))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private static Stream<Arguments> nullTransformationCases()
		{
			return Stream.of(
					NullTransformationCase.shape("transformFirst with null",
							() -> Dyad.of("storm", 3).transformSinister(null)),
					NullTransformationCase.shape("transformSecond with null",
							() -> Dyad.of("storm", 3).transformDexter((Function<Integer, Integer>) null))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		@FunctionalInterface
		private interface ThrowingInvocation
		{
			void invoke();
		}

		private record TransformFirstCase<R>(String as, Dyad<String, Integer> source,
		                                     Function<String, R> transformation, Dyad<R, Integer> expected)
		{
			private static <R> TransformFirstCase<R> shape(final String as, final Dyad<String, Integer> source,
			                                               final Function<String, R> transformation,
			                                               final Dyad<R, Integer> expected)
			{
				return new TransformFirstCase<>(as, source, transformation, expected);
			}
		}

		private record TransformSecondCase<R>(String as, Dyad<String, Integer> source,
		                                      Function<Integer, R> transformation, Dyad<String, R> expected)
		{
			private static <R> TransformSecondCase<R> shape(final String as, final Dyad<String, Integer> source,
			                                                final Function<Integer, R> transformation,
			                                                final Dyad<String, R> expected)
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

	@Nested
	@DisplayName("Conventional accessor aliases")
	final class ConventionalAccessors
	{
		@org.junit.jupiter.api.Test
		@DisplayName("first() returns sinister")
		void firstReturnsSinister()
		{
			assertThat(Dyad.of("a", 1).first()).as("first() == sinister").isEqualTo("a");
		}

		@org.junit.jupiter.api.Test
		@DisplayName("left() returns sinister")
		void leftReturnsSinister()
		{
			assertThat(Dyad.of("a", 1).left()).as("left() == sinister").isEqualTo("a");
		}

		@org.junit.jupiter.api.Test
		@DisplayName("second() returns dexter")
		void secondReturnsDexter()
		{
			assertThat(Dyad.of("a", 1).second()).as("second() == dexter").isEqualTo(1);
		}

		@org.junit.jupiter.api.Test
		@DisplayName("right() returns dexter")
		void rightReturnsDexter()
		{
			assertThat(Dyad.of("a", 1).right()).as("right() == dexter").isEqualTo(1);
		}

		@org.junit.jupiter.api.Test
		@DisplayName("mapFirst() transforms sinister — delegates to transformSinister()")
		void mapFirstTransformsSinister()
		{
			assertThat(Dyad.of("hello", 42).mapFirst(String::length))
					.as("mapFirst delegates to transformSinister")
					.isEqualTo(Dyad.of(5, 42));
		}

		@org.junit.jupiter.api.Test
		@DisplayName("mapLeft() transforms sinister — delegates to transformSinister()")
		void mapLeftTransformsSinister()
		{
			assertThat(Dyad.of("hello", 42).mapLeft(String::toUpperCase))
					.as("mapLeft delegates to transformSinister")
					.isEqualTo(Dyad.of("HELLO", 42));
		}

		@org.junit.jupiter.api.Test
		@DisplayName("mapSecond() transforms dexter — delegates to transformDexter()")
		void mapSecondTransformsDexter()
		{
			assertThat(Dyad.of("hello", 42).mapSecond(n -> n * 2))
					.as("mapSecond delegates to transformDexter")
					.isEqualTo(Dyad.of("hello", 84));
		}

		@org.junit.jupiter.api.Test
		@DisplayName("mapRight() transforms dexter — delegates to transformDexter()")
		void mapRightTransformsDexter()
		{
			assertThat(Dyad.of("hello", 42).mapRight(Object::toString))
					.as("mapRight delegates to transformDexter")
					.isEqualTo(Dyad.of("hello", "42"));
		}
	}
}