package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingWieldTest
{
	@Nested
	@DisplayName("Tests for wield() method")
	class WieldTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should produce expected Unfolding when value is present")
		@MethodSource("wieldPresentTestCases")
		<T, U> void testWieldWhenPresent(final String description, final Unfolding<T> source,
		                                 final Function<T, U> instrument,
		                                 final BiFunction<T, U, Unfolding<T>> wielding,
		                                 final Unfolding<T> expected)
		{
			assertThat(source.wield(instrument, wielding))
					.as("wield() for %s should produce %s", source, expected)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("should return chaos when empty")
		void testWieldWhenEmpty()
		{
			final Unfolding<String> source = Unfolding.chaos();

			assertThat(source.wield(t -> t.toUpperCase(), (t, u) -> Unfolding.beckon(t)))
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("should not invoke instrument or wielding when empty")
		void testWieldDoesNotInvokeWhenEmpty()
		{
			final Unfolding<String> source = Unfolding.chaos();
			final AtomicReference<String> sideEffect = new AtomicReference<>("untouched");

			source.<String>wield(
					t ->
					{
						sideEffect.set("instrument called");
						return t;
					},
					(t, u) ->
					{
						sideEffect.set("wielding called");
						return Unfolding.beckon(t);
					}
			);

			assertThat(sideEffect.get()).isEqualTo("untouched");
		}

		@Test
		@DisplayName("should preserve original value when handler returns it unchanged")
		void testWieldPreservesOriginal()
		{
			final Unfolding<String> source = Unfolding.beckon("hello");

			final Unfolding<String> result = source.wield(
					String::length,
					(t, len) -> Unfolding.beckon(t)
			);

			assertThat(result).isEqualTo(Unfolding.beckon("hello"));
		}

		@Test
		@DisplayName("should allow filtering based on intermediate value")
		void testWieldAsFilter()
		{
			final Unfolding<String> present = Unfolding.beckon("hello");
			final Unfolding<String> absent = Unfolding.beckon("hi");

			assertThat(present.wield(String::length, (t, len) -> len > 3 ? Unfolding.beckon(t) : Unfolding.chaos()))
					.isEqualTo(Unfolding.beckon("hello"));

			assertThat(absent.wield(String::length, (t, len) -> len > 3 ? Unfolding.beckon(t) : Unfolding.chaos()))
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("should allow conditional transform of T based on intermediate")
		void testWieldAsConditionalTransform()
		{
			final Unfolding<Integer> positive = Unfolding.beckon(5);
			final Unfolding<Integer> negative = Unfolding.beckon(-3);

			final Function<Integer, Boolean> instrument = n -> n > 0;
			final BiFunction<Integer, Boolean, Unfolding<Integer>> wielding =
					(n, isPositive) -> Unfolding.beckon(isPositive ? n * 2 : Math.abs(n));

			assertThat(positive.wield(instrument, wielding)).isEqualTo(Unfolding.beckon(10));
			assertThat(negative.wield(instrument, wielding)).isEqualTo(Unfolding.beckon(3));
		}

		@Test
		@DisplayName("should allow side effects on intermediate without losing T")
		void testWieldForSideEffect()
		{
			final Unfolding<String> source = Unfolding.beckon("hello");
			final AtomicReference<Integer> observed = new AtomicReference<>();

			final Unfolding<String> result = source.wield(
					String::length,
					(t, len) ->
					{
						observed.set(len);
						return Unfolding.beckon(t);
					}
			);

			assertThat(result).isEqualTo(Unfolding.beckon("hello"));
			assertThat(observed.get()).isEqualTo(5);
		}

		@Test
		@DisplayName("should treat null return from wielding as chaos")
		void testWieldNullHandlerReturnBecomesChoas()
		{
			final Unfolding<String> source = Unfolding.beckon("hello");

			assertThat(source.wield(String::length, (t, u) -> null))
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("should pass null intermediate to wielding when instrument returns null")
		void testWieldNullIntermediate()
		{
			final Unfolding<String> source = Unfolding.beckon("hello");
			final AtomicReference<Object> capturedIntermediate = new AtomicReference<>("sentinel");

			source.wield(
					_ -> null,
					(t, u) ->
					{
						capturedIntermediate.set(u);
						return Unfolding.beckon(t);
					}
			);

			assertThat(capturedIntermediate.get()).isNull();
		}

		@Test
		@DisplayName("should throw NullPointerException when instrument is null")
		void testWieldNullInstrument()
		{
			final Unfolding<String> source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.wield(null, (t, u) -> Unfolding.beckon(t)))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("instrument may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when wielding is null")
		void testWieldNullWielding()
		{
			final Unfolding<String> source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.wield(String::length, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("wielding may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException for null instrument before null wielding")
		void testWieldNullBothArguments()
		{
			final Unfolding<String> source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.wield(null, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("instrument may not be null");
		}

		private static Stream<Arguments> wieldPresentTestCases()
		{
			return Stream.of(
					new WieldTestCase<>("pass-through string",
							Unfolding.beckon("hello"),
							(Function<String, String>) t -> t.toUpperCase(),
							(t, u) -> Unfolding.beckon(t),
							Unfolding.beckon("hello")),

					new WieldTestCase<>("filter passes",
							Unfolding.beckon(10),
							(Function<Integer, Boolean>) n -> n % 2 == 0,
							(n, even) -> even ? Unfolding.beckon(n) : Unfolding.chaos(),
							Unfolding.beckon(10)),

					new WieldTestCase<>("filter blocks",
							Unfolding.beckon(7),
							(Function<Integer, Boolean>) n -> n % 2 == 0,
							(n, even) -> even ? Unfolding.beckon(n) : Unfolding.chaos(),
							Unfolding.chaos()),

					new WieldTestCase<>("transform on positive intermediate",
							Unfolding.beckon(4),
							(Function<Integer, Integer>) n -> n * n,
							(n, squared) -> Unfolding.beckon(n + squared),
							Unfolding.beckon(20)),

					new WieldTestCase<>("string: use length as routing key",
							Unfolding.beckon("hi"),
							(Function<String, Integer>) String::length,
							(t, len) -> Unfolding.beckon(len > 4 ? t.toUpperCase() : t.toLowerCase()),
							Unfolding.beckon("hi")),

					new WieldTestCase<>("empty source stays empty",
							Unfolding.chaos(),
							(Function<String, String>) t -> t.toUpperCase(),
							(t, u) -> Unfolding.beckon(t),
							Unfolding.chaos())
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.instrument(), tc.wielding(), tc.expected()));
		}

		private record WieldTestCase<T, U>(String description, Unfolding<T> source, Function<T, U> instrument,
		                                   BiFunction<T, U, Unfolding<T>> wielding, Unfolding<T> expected)
		{
		}
	}
}