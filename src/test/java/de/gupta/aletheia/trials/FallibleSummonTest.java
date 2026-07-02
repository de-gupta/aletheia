package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Fallible#summon")
final class FallibleSummonTest
{
	@Nested
	@DisplayName("when Fallible is a success")
	final class WhenSuccess
	{
		@Test
		@DisplayName("returns the success value")
		void returnsSuccessValue()
		{
			assertThat(Fallible.success("hello").summon())
					.as("summon on success returns the value")
					.isEqualTo("hello");
		}

		@Test
		@DisplayName("get() delegates to summon()")
		void getDelegatesToSummon()
		{
			final Fallible<Integer> fallible = Fallible.success(42);

			assertThat(fallible.get())
					.as("get() must equal summon()")
					.isEqualTo(fallible.summon());
		}

		@Test
		@DisplayName("works with null success value")
		void worksWithNullSuccessValue()
		{
			assertThat(Fallible.success(null).summon())
					.as("null success value returned as-is")
					.isNull();
		}
	}

	@Nested
	@DisplayName("when Fallible is a failure")
	final class WhenFailure
	{
		@Test
		@DisplayName("rethrows RuntimeException directly — no wrapping")
		void rethrowsRuntimeExceptionDirectly()
		{
			final var original = new IllegalArgumentException("bad input");
			final Fallible<String> fallible = Fallible.failure(original);

			assertThatThrownBy(fallible::summon)
					.as("RuntimeException rethrown without wrapping")
					.isSameAs(original);
		}

		@Test
		@DisplayName("rethrows checked exception via sneaky throw — no cast required")
		void rethrowsCheckedExceptionViaSneakyThrow()
		{
			final var checked = new java.io.IOException("disk error");
			final Fallible<String> fallible = Fallible.failure(checked);

			assertThatThrownBy(fallible::summon)
					.as("checked exception rethrown as-is via sneaky throw")
					.isSameAs(checked)
					.isInstanceOf(java.io.IOException.class);
		}

		@Test
		@DisplayName("get() on failure rethrows the same exception")
		void getOnFailureRethrows()
		{
			final var original = new IllegalStateException("failure");
			final Fallible<String> fallible = Fallible.failure(original);

			assertThatThrownBy(fallible::get)
					.as("get() on failure rethrows")
					.isSameAs(original);
		}

		@Test
		@DisplayName("after metamorphose with unmatched portent — summon rethrows the original exception")
		void afterMetamorphoseWithUnmatchedPortentSummonRethrows()
		{
			final var original = new IllegalArgumentException("unhandled");

			final Fallible<String> result = Fallible.<String>failure(original)
			                                        .metamorphose(
															String::toUpperCase,
															java.util.List.of(Portent.on(java.io.IOException.class,
							                                        _ -> "io fallback")));

			assertThatThrownBy(result::summon)
					.as("unmatched exception propagates through summon")
					.isSameAs(original);
		}

		@Test
		@DisplayName("replaces coronate(identity, rethrow) idiom")
		void replacesCulminatIdentityRethrowIdiom()
		{
			final var exception = new IllegalStateException("bang");

			assertThatThrownBy(() ->
					Fallible.<String>failure(exception)
					        .summon())
					.as("summon replaces coronate(identity, fury -> { throw fury; })")
					.isSameAs(exception);
		}
	}
}