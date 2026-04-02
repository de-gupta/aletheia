package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade abyss tests")
final class CascadeAbyssTest
{
	@Nested
	@DisplayName("Aspects of abyss identity")
	final class IdentityTests
	{
		@Test
		@DisplayName("should return the same nadir instance for every invocation")
		void shouldReturnSameNadirInstanceForEveryInvocation()
		{
			var first = Cascade.abyss();
			var second = Cascade.abyss();

			assertThat(first)
					.as("abyss should always unveil singleton nadir")
					.isSameAs(second)
					.isInstanceOf(Nadir.class);
		}
	}

	@Nested
	@DisplayName("Aspects of abyss behavior")
	final class BehaviorTests
	{
		@Test
		@DisplayName("should be sterile and unsummonable")
		void shouldBeSterileAndUnsummonable()
		{
			var abyss = Cascade.<String>abyss();

			assertThat(abyss.sterile())
					.as("abyss should be sterile")
					.isTrue();
			assertThat(abyss.supple())
					.as("abyss should not be supple")
					.isFalse();

			assertThatThrownBy(abyss::summon)
					.as("summon on abyss should raise EmptyCascadeException")
					.isInstanceOf(EmptyCascadeException.class);
		}
	}
}
