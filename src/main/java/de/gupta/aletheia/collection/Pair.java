package de.gupta.aletheia.collection;

import java.util.function.Function;

public record Pair<A, B>(A first, B second)
{
	public <C> Pair<C, B> transformFirst(final Function<A, C> transformation)
	{
		return of(transformation.apply(first), second);
	}

	public static <A, B> Pair<A, B> of(final A first, final B second)
	{
		return new Pair<>(first, second);
	}

	public <C> Pair<A, C> transformSecond(final Function<B, C> transformation)
	{
		return of(first, transformation.apply(second));
	}
}