package de.gupta.aletheia.collection;

import java.util.function.Function;

public record Dyad<A, B>(A sinister, B dexter)
{
	public static <A, B> Dyad<A, B> of(final A first, final B second)
	{
		return new Dyad<>(first, second);
	}

	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard Pair-style accessors and transforms. Each delegates to its mythic equivalent below.

	public A first()
	{
		return sinister;
	}

	public A left()
	{
		return sinister;
	}

	public B second()
	{
		return dexter;
	}

	public B right()
	{
		return dexter;
	}

	public <C> Dyad<C, B> mapFirst(final Function<A, C> mapper)
	{
		return transformSinister(mapper);
	}

	public <C> Dyad<C, B> mapLeft(final Function<A, C> mapper)
	{
		return transformSinister(mapper);
	}

	public <C> Dyad<A, C> mapSecond(final Function<B, C> mapper)
	{
		return transformDexter(mapper);
	}

	public <C> Dyad<A, C> mapRight(final Function<B, C> mapper)
	{
		return transformDexter(mapper);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.
	// sinister() and dexter() are auto-generated record accessors.

	public <C> Dyad<C, B> transformSinister(final Function<A, C> transformation)
	{
		return of(transformation.apply(sinister), dexter);
	}

	public <C> Dyad<A, C> transformDexter(final Function<B, C> transformation)
	{
		return of(sinister, transformation.apply(dexter));
	}
}