package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmptyCascadeException")
final class EmptyCascadeExceptionTest
{
	@Test
	@DisplayName("instance() carries default message")
	void instanceCarriesDefaultMessage()
	{
		assertThat(EmptyCascadeException.instance().getMessage())
				.as("default message")
				.isNotBlank();
	}

	@Test
	@DisplayName("withMessage() carries the supplied message")
	void withMessageCarriesSuppliedMessage()
	{
		final var ex = EmptyCascadeException.withMessage("custom error");

		assertThat(ex.getMessage())
				.as("custom message preserved")
				.isEqualTo("custom error");
	}

	@Test
	@DisplayName("withMessage() and instance() produce distinct messages")
	void withMessageProducesDistinctMessage()
	{
		assertThat(EmptyCascadeException.withMessage("override").getMessage())
				.as("withMessage differs from default")
				.isNotEqualTo(EmptyCascadeException.instance().getMessage());
	}
}
