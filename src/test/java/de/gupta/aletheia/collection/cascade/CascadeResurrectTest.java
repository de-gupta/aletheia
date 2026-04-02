package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade resurrect tests")
final class CascadeResurrectTest
{
	@Nested
	@DisplayName("Aspects of grace application")
	final class GraceApplicationTests
	{
		@Test
		@DisplayName("should ignore grace for brook and keep self")
		void shouldIgnoreGraceForBrookAndKeepSelf()
		{
			var called = new AtomicBoolean(false);
			var source = Cascade.beckon("alive");

			var result = source.resurrect(() ->
			{
				called.set(true);
				return Cascade.beckon("unused");
			});

			assertThat(called.get()).isFalse();
			assertThat(result).isSameAs(source);
		}

		@Test
		@DisplayName("should use grace for abyss")
		void shouldUseGraceForAbyss()
		{
			var result = Cascade.<String>abyss().resurrect(() -> Cascade.beckon("reborn"));

			assertThat(result.summon()).containsExactly("reborn");
		}

		@Test
		@DisplayName("should remain abyss when grace returns null")
		void shouldRemainAbyssWhenGraceReturnsNull()
		{
			var result = Cascade.<String>abyss().resurrect(() -> null);

			assertThat(result).isSameAs(Cascade.abyss());
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
			assertThatThrownBy(() -> Cascade.beckon("x").resurrect(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("grace may not be null");
		}

		@Test
		@DisplayName("should reject null grace for abyss")
		void shouldRejectNullGraceForAbyss()
		{
			assertThatThrownBy(() -> Cascade.<String>abyss().resurrect(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("grace may not be null");
		}
	}
}
