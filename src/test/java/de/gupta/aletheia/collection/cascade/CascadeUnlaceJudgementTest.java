package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade unlace(judgement, consumer) tests")
final class CascadeUnlaceJudgementTest
{
	@Nested
	@DisplayName("Aspects of selective side effects")
	final class SelectiveSideEffectTests
	{
		@Test
		@DisplayName("should invoke consumer only for judged elements")
		void shouldInvokeConsumerOnlyForJudgedElements()
		{
			var seen = new ArrayList<Integer>();
			var result = Cascade.beckon(1, 2, 3, 4).unlace(n -> n % 2 == 0, seen::add);

			assertThat(seen).isEmpty();
			assertThat(result.summon()).containsExactly(1, 2, 3, 4);
			assertThat(seen).containsExactly(2, 4);
		}

		@Test
		@DisplayName("should remain nadir for abyss even with null args")
		void shouldRemainNadirForAbyssEvenWithNullArgs()
		{
			assertThat(Cascade.<Integer>abyss().unlace(null, null)).isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of argument guards")
	final class ArgumentGuardTests
	{
		@Test
		@DisplayName("should reject null judgement for brook")
		void shouldRejectNullJudgementForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).unlace(null, _ ->
			{
			}))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		@Test
		@DisplayName("should reject null consumer for brook")
		void shouldRejectNullConsumerForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).unlace(_ -> true, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("impregnator may not be null");
		}
	}
}
