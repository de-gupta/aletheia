package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Nadir instance tests")
final class NadirInstanceTest
{
	@Nested
	@DisplayName("Aspects of singleton identity")
	final class SingletonIdentityTests
	{
		@Test
		@DisplayName("should return same object across different generic calls")
		void shouldReturnSameObjectAcrossDifferentGenericCalls()
		{
			Cascade<String> strings = Nadir.instance();
			Cascade<Integer> numbers = Nadir.instance();

			assertThat(strings)
					.as("nadir instance should be singleton regardless of type")
					.isSameAs(numbers)
					.isInstanceOf(Nadir.class);
		}
	}

	@Nested
	@DisplayName("Aspects of abyssal traits")
	final class AbyssalTraitTests
	{
		@Test
		@DisplayName("should exhibit canonical nadir state")
		void shouldExhibitCanonicalNadirState()
		{
			var nadir = Nadir.instance();

			assertThat(nadir.sterile())
					.as("nadir should always be sterile")
					.isTrue();
			assertThat(nadir.supple())
					.as("nadir should never be supple")
					.isFalse();
			assertThat(nadir.toString())
					.as("nadir toString should reveal empty cascade glyph")
					.isEqualTo("Cascade{}");
		}
	}
}
