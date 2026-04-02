package de.gupta.aletheia.collection.cascade;

public final class EmptyCascadeException extends RuntimeException
{
	private static final String DEFAULT_MESSAGE = "An empty cascade cannot pour forth its current";

	public static EmptyCascadeException instance()
	{
		return new EmptyCascadeException(DEFAULT_MESSAGE);
	}

	public static EmptyCascadeException withMessage(final String message)
	{
		return new EmptyCascadeException(message);
	}

	private EmptyCascadeException(final String message)
	{
		super(message);
	}
}
