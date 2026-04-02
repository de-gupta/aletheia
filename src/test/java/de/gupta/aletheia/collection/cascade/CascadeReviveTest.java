package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade revive tests")
final class CascadeReviveTest
{
	@Nested
	@DisplayName("Aspects of revival behavior")
	final class RevivalBehaviorTests
	{
		@Test
		@DisplayName("should ignore grace for brook and keep self")
		void shouldIgnoreGraceForBrookAndKeepSelf()
		{
			var called = new AtomicBoolean(false);
			var source = Cascade.beckon("alive");

			var result = source.revive(() ->
			{
				called.set(true);
				return "unused";
			});

			assertThat(called.get()).isFalse();
			assertThat(result).isSameAs(source);
		}

		@Test
		@DisplayName("should create brook from abyss when grace yields value")
		void shouldCreateBrookFromAbyssWhenGraceYieldsValue()
		{
			var result = Cascade.<String>abyss().revive(() -> "reborn");

			assertThat(result.summon()).containsExactly("reborn");
		}

		@Test
		@DisplayName("should create brook carrying null when grace yields null")
		void shouldCreateBrookCarryingNullWhenGraceYieldsNull()
		{
			var result = Cascade.<String>abyss().revive(() -> null);

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.summon()).containsExactly((String) null);
		}
	}

	@Nested
	@DisplayName("Aspects of grace guard")
	final class GraceGuardTests
	{
		@Test
		@DisplayName("should reject null grace for brook")
		void shouldRejectNullGraceForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("x").revive(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("grace may not be null");
		}

		@Test
		@DisplayName("should reject null grace for abyss")
		void shouldRejectNullGraceForAbyss()
		{
			assertThatThrownBy(() -> Cascade.<String>abyss().revive(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("grace may not be null");
		}
	}
}
