package de.gupta.aletheia.collection;

import java.util.function.Function;

public record Dyad<A, B>(A sinister, B dexter)
{
	public static <A, B> Dyad<A, B> of(final A first, final B second)
	{
		return new Dyad<>(first, second);
	}

	public <C> Dyad<C, B> transformSinister(final Function<A, C> transformation)
	{
		return of(transformation.apply(sinister), dexter);
	}

	public <C> Dyad<A, C> transformDexter(final Function<B, C> transformation)
	{
		return of(sinister, transformation.apply(dexter));
	}
}