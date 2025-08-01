package de.gupta.aletheia.functional;

public final class EmptyUnfoldingException extends RuntimeException
{
	private static final String DEFAULT_MESSAGE = "An empty vessel cannot pour forth wisdom";
	private static final EmptyUnfoldingException DEFAULT_INSTANCE = new EmptyUnfoldingException(DEFAULT_MESSAGE);

	public static EmptyUnfoldingException instance()
	{
		return DEFAULT_INSTANCE;
	}

	public static EmptyUnfoldingException withMessage(final String message)
	{
		return new EmptyUnfoldingException(message);
	}

	private EmptyUnfoldingException(String message)
	{
		super(message);
	}
}