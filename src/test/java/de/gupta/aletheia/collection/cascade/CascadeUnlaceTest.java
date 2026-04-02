package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade unlace tests")
final class CascadeUnlaceTest
{
	@Nested
	@DisplayName("Aspects of side effects")
	final class SideEffectTests
	{
		@Test
		@DisplayName("should execute consumer once per element when realized")
		void shouldExecuteConsumerOncePerElementWhenRealized()
		{
			var seen = new ArrayList<Integer>();

			var result = Cascade.beckon(1, 2, 3).unlace(seen::add);

			assertThat(seen).isEmpty();
			assertThat(result.summon()).containsExactly(1, 2, 3);
			assertThat(seen).containsExactly(1, 2, 3);
		}

		@Test
		@DisplayName("should preserve original current while peeking")
		void shouldPreserveOriginalCurrentWhilePeeking()
		{
			var seen = new ArrayList<String>();
			var result = Cascade.beckon("north", "south").unlace(seen::add);

			assertThat(result.summon()).containsExactly("north", "south");
			assertThat(seen).containsExactly("north", "south");
		}

		@Test
		@DisplayName("should remain nadir for abyss even with null consumer")
		void shouldRemainNadirForAbyssEvenWithNullConsumer()
		{
			assertThat(Cascade.<Integer>abyss().unlace(null)).isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of consumer guard")
	final class ConsumerGuardTests
	{
		@Test
		@DisplayName("should reject null consumer for brook")
		void shouldRejectNullConsumerForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).unlace(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("impregnator may not be null");
		}
	}
}