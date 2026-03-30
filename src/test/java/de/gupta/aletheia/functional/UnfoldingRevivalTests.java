package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingRevivalTests
{
	private record TestRecord(String name, int age)
	{
		static TestRecord of(final String name, final int age)
		{
			return new TestRecord(name, age);
		}
	}

	@Nested
	@DisplayName("Tests for revive() method")
	class ReviveTests
	{
		@DisplayName("should return the same instance when a myth")
		@ParameterizedTest(name = "{0}")
		@MethodSource("mythicAlternates")
		<U> void shouldReturnTheSameInstanceWhenMyth(final String description, final Unfolding<U> myth,
													 final U alternate)
		{
			assertThat(myth.revive(() -> alternate))
					.as("revive() should return the same instance when a myth")
					.isSameAs(myth);
		}

		@DisplayName("should return a new instance when not a myth")
		@ParameterizedTest(name = "{0}")
		@MethodSource("shellAlternates")
		<U> void shouldReturnANewInstanceWhenNotMyth(final String description, final U alternate)
		{
			final Unfolding<U> shell = Unfolding.chaos();

			assertThat(shell.revive(() -> alternate).sterile())
					.as("revive() should return a new non-sterile instance when not a myth")
					.isFalse();

			assertThat(shell.revive(() -> alternate).summon())
					.as("revive() should return a new instance when chaos")
					.isSameAs(alternate);
		}

		@DisplayName("should throw exception for null supplier")
		@Test
		void shouldThrowExceptionForNullSupplier()
		{
			final Unfolding<String> shell = Unfolding.chaos();

			assertThatThrownBy(() -> shell.revive(null))
					.as("revive() with null supplier should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("grace may not be null");
		}

		private static Stream<Arguments> mythicAlternates()
		{
			return Stream.of(
					MythicAlternateTestCase.of("same alternate", Unfolding.beckon("hello"), "hello"),
					MythicAlternateTestCase.of("different alternate", Unfolding.beckon("hello"), "world"),
					MythicAlternateTestCase.of("integers", Unfolding.beckon(1), 2),
					MythicAlternateTestCase.of("Doubles", Unfolding.beckon(1.0), 2.0),
					MythicAlternateTestCase.of("custom object", Unfolding.beckon(TestRecord.of("John", 30)),
							TestRecord.of("Jane", 25))
			).map(tc -> Arguments.of(tc.description, tc.myth, tc.alternate));
		}

		private static Stream<Arguments> shellAlternates()
		{
			return Stream.of(
					ShellAlternateTestCase.of("string", "hello"),
					ShellAlternateTestCase.of("integer", 2),
					ShellAlternateTestCase.of("Double", 2.0),
					ShellAlternateTestCase.of("custom object", TestRecord.of("Jane", 25))
			).map(tc -> Arguments.of(tc.description, tc.alternate));
		}

		private record MythicAlternateTestCase<U>(String description, Unfolding<U> myth, U alternate)
		{
			static <U> MythicAlternateTestCase<U> of(final String description, final Unfolding<U> myth,
													 final U alternate)
			{
				return new MythicAlternateTestCase<>(description, myth, alternate);
			}
		}

		private record ShellAlternateTestCase<U>(String description, U alternate)
		{
			static <U> ShellAlternateTestCase<U> of(final String description, final U alternate)
			{
				return new ShellAlternateTestCase<>(description, alternate);
			}
		}

	}
}