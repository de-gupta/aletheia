package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#smelt(identity, operation)")
final class CascadeSmeltIdentityTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("smeltWithIdentityCases")
		@DisplayName("reduces all elements using the operation, starting from identity")
		void smeltWithIdentity(final String as, final SmeltCase tc)
		{
			assertThat(tc.source().smelt(tc.identity(), tc.operation()))
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("identity is not used when elements are present — only as neutral starting element")
		void identityOnlyUsedWhenNecessary()
		{
			assertThat(Cascade.beckon(1, 2, 3).smelt(0, Integer::sum))
					.as("0 + 1 + 2 + 3 = 6")
					.isEqualTo(6);
		}

		@Test
		@DisplayName("single element — returns operation(identity, element)")
		void singleElementReturnsCombinedWithIdentity()
		{
			assertThat(Cascade.beckon(5).smelt(0, Integer::sum))
					.as("0 + 5 = 5")
					.isEqualTo(5);
		}

		@Test
		@DisplayName("string concatenation with identity")
		void stringConcatenationWithIdentity()
		{
			assertThat(Cascade.beckon("b", "c", "d").smelt("a", String::concat))
					.as("abcd concatenated")
					.isEqualTo("abcd");
		}

		@Test
		@DisplayName("reduce(identity, op) delegates to smelt(identity, op)")
		void reduceDelegatesToSmelt()
		{
			final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4);

			assertThat(source.reduce(0, Integer::sum))
					.as("reduce(identity, op) must equal smelt(identity, op)")
					.isEqualTo(source.smelt(0, Integer::sum));
		}

		@Test
		@DisplayName("throws when operation is null")
		void throwsWhenOperationIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).smelt(0, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("operation may not be null");
		}

		private static Stream<Arguments> smeltWithIdentityCases()
		{
			return Stream.of(
					new SmeltCase("sum of integers", Cascade.beckon(1, 2, 3, 4, 5), 0, Integer::sum, 15),
					new SmeltCase("product of integers", Cascade.beckon(1, 2, 3, 4), 1, (a, b) -> a * b, 24),
					new SmeltCase("max of integers", Cascade.beckon(3, 1, 4, 1, 5, 9), Integer.MIN_VALUE,
							Integer::max, 9),
					new SmeltCase("min of integers", Cascade.beckon(3, 1, 4, 1, 5, 9), Integer.MAX_VALUE,
							Integer::min, 1)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SmeltCase(String as, Cascade<Integer> source, Integer identity,
		                         BinaryOperator<Integer> operation, Integer expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns the identity value — never throws")
		void returnsIdentityValue()
		{
			assertThat(Cascade.<Integer>abyss().smelt(42, Integer::sum))
					.as("empty cascade smelt returns identity")
					.isEqualTo(42);
		}

		@Test
		@DisplayName("returns identity regardless of operation — operation never called")
		void returnsIdentityRegardlessOfOperation()
		{
			assertThat(Cascade.<Integer>abyss().smelt(99, (_, _) ->
			{
				throw new AssertionError("operation must not be called on empty cascade");
			}))
					.as("identity returned without invoking operation")
					.isEqualTo(99);
		}

		@Test
		@DisplayName("contrast with smelt(BiFunction) which returns empty Unfolding on empty cascade")
		void contrastWithNonIdentitySmelt()
		{
			final Cascade<Integer> empty = Cascade.abyss();

			assertThat(empty.smelt(0, Integer::sum))
					.as("smelt(identity, op) always returns a value")
					.isEqualTo(0);

			assertThat(empty.smelt(Integer::sum).sterile())
					.as("smelt(BiFunction) on empty returns empty Unfolding")
					.isTrue();
		}
	}
}