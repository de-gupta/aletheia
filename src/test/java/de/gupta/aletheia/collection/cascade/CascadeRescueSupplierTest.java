package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade infuse(supplier) tests")
final class CascadeInfuseSupplierTest
{
	@Nested
	@DisplayName("Aspects of revelation choice")
	final class RevelationChoiceTests
	{
		@Test
		@DisplayName("should ignore revelation for brook and return summon")
		void shouldIgnoreRevelationForBrookAndReturnSummon()
		{
			var called = new AtomicBoolean(false);

			var result = Cascade.beckon("a", "b").infuse(() ->
			{
				called.set(true);
				return List.of("x");
			});

			assertThat(called.get()).isFalse();
			assertThat(result).containsExactly("a", "b");
		}

		@Test
		@DisplayName("should invoke revelation for abyss and return it unchanged")
		void shouldInvokeRevelationForAbyssAndReturnItUnchanged()
		{
			var fallback = new ArrayList<>(List.of("x", "y"));
			var result = Cascade.<String>abyss().infuse(() -> fallback);

			assertThat(result).isSameAs(fallback);
		}
	}

	@Nested
	@DisplayName("Aspects of supplier guard")
	final class SupplierGuardTests
	{
		@Test
		@DisplayName("should reject null supplier for brook")
		void shouldRejectNullSupplierForBrook()
		{
			Supplier<? extends java.util.Collection<? extends Integer>> revelation = null;

			assertThatThrownBy(() -> Cascade.beckon(1).infuse(revelation))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("revelation may not be null");
		}

		@Test
		@DisplayName("should reject null supplier for abyss")
		void shouldRejectNullSupplierForAbyss()
		{
			Supplier<? extends java.util.Collection<? extends Integer>> revelation = null;

			assertThatThrownBy(() -> Cascade.<Integer>abyss().infuse(revelation))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("revelation may not be null");
		}
	}
}
