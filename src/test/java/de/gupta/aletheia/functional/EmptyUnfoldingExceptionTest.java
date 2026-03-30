package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmptyUnfoldingException tests")
final class EmptyUnfoldingExceptionTest
{
	@Test
	@DisplayName("instance() should return a fresh exception each time")
	void instanceShouldReturnAFreshExceptionEachTime()
	{
		EmptyUnfoldingException first = EmptyUnfoldingException.instance();
		EmptyUnfoldingException second = EmptyUnfoldingException.instance();

		assertThat(first).isNotSameAs(second);
		assertThat(first).hasMessage("An empty vessel cannot pour forth wisdom");
		assertThat(second).hasMessage("An empty vessel cannot pour forth wisdom");
	}

	@Test
	@DisplayName("withMessage() should return a fresh exception with the supplied message")
	void withMessageShouldReturnAFreshExceptionWithTheSuppliedMessage()
	{
		EmptyUnfoldingException first = EmptyUnfoldingException.withMessage("first");
		EmptyUnfoldingException second = EmptyUnfoldingException.withMessage("second");

		assertThat(first).isNotSameAs(second);
		assertThat(first).hasMessage("first");
		assertThat(second).hasMessage("second");
	}
}
