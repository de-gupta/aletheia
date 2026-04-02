package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade forge tests")
final class CascadeForgeTest
{
	@Nested
	@DisplayName("Aspects of forged reduction")
	final class ForgedReductionTests
	{
		@Test
		@DisplayName("should reduce brook into unfolding result")
		void shouldReduceBrookIntoUnfoldingResult()
		{
			var result = Cascade.beckon(1, 2, 3).forge(Integer::sum);

			assertThat(result.summon())
					.as("forge(sum) should yield summed unfolding")
					.isEqualTo(6);
		}

		@Test
		@DisplayName("should produce sterile unfolding for abyss")
		void shouldProduceSterileUnfoldingForAbyss()
		{
			var result = Cascade.<Integer>abyss().forge(Integer::sum);

			assertThat(result.sterile())
					.as("forging abyss should remain empty")
					.isTrue();
		}
	}

	@Nested
	@DisplayName("Aspects of operation guard")
	final class OperationGuardTests
	{
		@Test
		@DisplayName("should reject null operation for brook")
		void shouldRejectNullOperationForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).forge(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("operation may not be null");
		}

		@Test
		@DisplayName("should reject null operation for abyss")
		void shouldRejectNullOperationForAbyss()
		{
			assertThatThrownBy(() -> Cascade.<Integer>abyss().forge(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("operation may not be null");
		}
	}
}
