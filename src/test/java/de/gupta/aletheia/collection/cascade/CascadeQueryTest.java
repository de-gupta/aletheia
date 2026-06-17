package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.functional.Unfolding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade query methods — affirm, permit, deny, harbor, seek")
final class CascadeQueryTest
{
	@Nested
	@DisplayName("affirm — all elements satisfy predicate")
	final class Affirm
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("affirmCases")
		@DisplayName("returns true when all elements satisfy the predicate")
		void returnsTrueWhenAllSatisfy(final String as, final Cascade<Integer> source,
		                               final Predicate<Integer> judgement, final boolean expected)
		{
			assertThat(source.affirm(judgement)).as(as).isEqualTo(expected);
		}

		@Test
		@DisplayName("all() delegates to affirm()")
		void allDelegatesToAffirm()
		{
			final Cascade<Integer> cascade = Cascade.beckon(2, 4, 6);
			assertThat(cascade.all(n -> n % 2 == 0))
					.as("all() must equal affirm()")
					.isEqualTo(cascade.affirm(n -> n % 2 == 0));
		}

		@Test
		@DisplayName("returns true vacuously when Cascade is empty")
		void returnsTrueVacuouslyWhenEmpty()
		{
			assertThat(Cascade.<Integer>abyss().affirm(n -> n > 0))
					.as("affirm on empty cascade is vacuously true")
					.isTrue();
		}

		@Test
		@DisplayName("throws when judgement is null")
		void throwsWhenJudgementIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).affirm(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		private static Stream<Arguments> affirmCases()
		{
			return Stream.of(
					Arguments.of("all even — true", Cascade.beckon(2, 4, 6), (Predicate<Integer>) n -> n % 2 == 0,
							true),
					Arguments.of("one odd breaks it — false", Cascade.beckon(2, 3, 6),
							(Predicate<Integer>) n -> n % 2 == 0, false),
					Arguments.of("all positive — true", Cascade.beckon(1, 2, 3), (Predicate<Integer>) n -> n > 0, true),
					Arguments.of("single non-matching — false", Cascade.beckon(5), (Predicate<Integer>) n -> n < 0,
							false)
			);
		}
	}

	@Nested
	@DisplayName("permit — any element satisfies predicate")
	final class Permit
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("permitCases")
		@DisplayName("returns true when at least one element satisfies the predicate")
		void returnsTrueWhenAnyMatches(final String as, final Cascade<Integer> source,
		                               final Predicate<Integer> judgement, final boolean expected)
		{
			assertThat(source.permit(judgement)).as(as).isEqualTo(expected);
		}

		@Test
		@DisplayName("any() delegates to permit()")
		void anyDelegatesToPermit()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 3, 5);
			assertThat(cascade.any(n -> n % 2 == 0))
					.as("any() must equal permit()")
					.isEqualTo(cascade.permit(n -> n % 2 == 0));
		}

		@Test
		@DisplayName("returns false when Cascade is empty")
		void returnsFalseWhenEmpty()
		{
			assertThat(Cascade.<Integer>abyss().permit(n -> n > 0))
					.as("permit on empty cascade is false")
					.isFalse();
		}

		@Test
		@DisplayName("throws when judgement is null")
		void throwsWhenJudgementIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).permit(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		private static Stream<Arguments> permitCases()
		{
			return Stream.of(
					Arguments.of("one even among odds — true", Cascade.beckon(1, 2, 3),
							(Predicate<Integer>) n -> n % 2 == 0, true),
					Arguments.of("no evens — false", Cascade.beckon(1, 3, 5), (Predicate<Integer>) n -> n % 2 == 0,
							false),
					Arguments.of("single match — true", Cascade.beckon(7), (Predicate<Integer>) n -> n == 7, true),
					Arguments.of("all match — true", Cascade.beckon(2, 4), (Predicate<Integer>) n -> n % 2 == 0, true)
			);
		}
	}

	@Nested
	@DisplayName("deny — no element satisfies predicate")
	final class Deny
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("denyCases")
		@DisplayName("returns true when no element satisfies the predicate")
		void returnsTrueWhenNoneMatch(final String as, final Cascade<Integer> source,
		                              final Predicate<Integer> judgement, final boolean expected)
		{
			assertThat(source.deny(judgement)).as(as).isEqualTo(expected);
		}

		@Test
		@DisplayName("none() delegates to deny()")
		void noneDelegatesToDeny()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 3, 5);
			assertThat(cascade.none(n -> n % 2 == 0))
					.as("none() must equal deny()")
					.isEqualTo(cascade.deny(n -> n % 2 == 0));
		}

		@Test
		@DisplayName("returns true vacuously when Cascade is empty")
		void returnsTrueVacuouslyWhenEmpty()
		{
			assertThat(Cascade.<Integer>abyss().deny(n -> n > 0))
					.as("deny on empty cascade is vacuously true")
					.isTrue();
		}

		@Test
		@DisplayName("throws when judgement is null")
		void throwsWhenJudgementIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).deny(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		private static Stream<Arguments> denyCases()
		{
			return Stream.of(
					Arguments.of("no evens — true", Cascade.beckon(1, 3, 5), (Predicate<Integer>) n -> n % 2 == 0,
							true),
					Arguments.of("one even breaks it — false", Cascade.beckon(1, 2, 3),
							(Predicate<Integer>) n -> n % 2 == 0, false),
					Arguments.of("single non-matching — true", Cascade.beckon(7), (Predicate<Integer>) n -> n < 0,
							true),
					Arguments.of("single match — false", Cascade.beckon(7), (Predicate<Integer>) n -> n == 7, false)
			);
		}
	}

	@Nested
	@DisplayName("harbor — cascade contains element")
	final class Harbor
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("harborCases")
		@DisplayName("returns true when element is present in the cascade")
		void returnsTrueWhenElementPresent(final String as, final Cascade<Integer> source,
		                                   final Integer element, final boolean expected)
		{
			assertThat(source.harbor(element)).as(as).isEqualTo(expected);
		}

		@Test
		@DisplayName("contains() delegates to harbor()")
		void containsDelegatesToHarbor()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3);
			assertThat(cascade.contains(2))
					.as("contains() must equal harbor()")
					.isEqualTo(cascade.harbor(2));
		}

		@Test
		@DisplayName("returns false when Cascade is empty")
		void returnsFalseWhenEmpty()
		{
			assertThat(Cascade.<Integer>abyss().harbor(1))
					.as("harbor on empty cascade is false")
					.isFalse();
		}

		private static Stream<Arguments> harborCases()
		{
			return Stream.of(
					Arguments.of("element is present — true", Cascade.beckon(1, 2, 3), 2, true),
					Arguments.of("element is absent — false", Cascade.beckon(1, 2, 3), 9, false),
					Arguments.of("first element — true", Cascade.beckon(5, 6, 7), 5, true),
					Arguments.of("last element — true", Cascade.beckon(5, 6, 7), 7, true)
			);
		}
	}

	@Nested
	@DisplayName("seek — find first matching element")
	final class Seek
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("seekCases")
		@DisplayName("returns the first element satisfying the predicate")
		void returnsFirstMatchingElement(final String as, final SeekCase tc)
		{
			assertThat(tc.source().seek(tc.judgement()))
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("find() delegates to seek()")
		void findDelegatesToSeek()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3, 4);
			assertThat(cascade.find(n -> n % 2 == 0))
					.as("find() must equal seek()")
					.isEqualTo(cascade.seek(n -> n % 2 == 0));
		}

		@Test
		@DisplayName("returns empty when Cascade is empty")
		void returnsEmptyWhenCascadeIsEmpty()
		{
			assertThat(Cascade.<Integer>abyss().seek(n -> n > 0))
					.as("seek on empty cascade")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("returns empty when no element matches")
		void returnsEmptyWhenNoMatch()
		{
			assertThat(Cascade.beckon(1, 3, 5).seek(n -> n % 2 == 0))
					.as("no even numbers found")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("throws when judgement is null")
		void throwsWhenJudgementIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).seek(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		private static Stream<Arguments> seekCases()
		{
			return Stream.of(
					new SeekCase("first even in mixed list",
							Cascade.beckon(1, 2, 3, 4), n -> n % 2 == 0, Unfolding.beckon(2)),
					new SeekCase("first negative",
							Cascade.beckon(3, -1, 5, -2), n -> n < 0, Unfolding.beckon(-1)),
					new SeekCase("single matching element",
							Cascade.beckon(7), n -> n == 7, Unfolding.beckon(7)),
					new SeekCase("no match returns empty",
							Cascade.beckon(1, 3, 5), n -> n < 0, Unfolding.chaos())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SeekCase(String as, Cascade<Integer> source, Predicate<Integer> judgement,
		                        Unfolding<Integer> expected)
		{
		}
	}
}