package de.gupta.aletheia.collection;

public record Triad<A, B, C>(A dawn, B zenith, C dusk)
{
	public static <A, B, C> Triad<A, B, C> of(A dawn, B zenith, C dusk)
	{
		return new Triad<>(dawn, zenith, dusk);
	}

	public static <A, B, C> Triad<A, B, C> of(A dawn, Dyad<B, C> second)
	{
		return of(dawn, second.sinister(), second.dexter());
	}

	public static <A, B, C> Triad<A, B, C> of(Dyad<A, B> first, C dusk)
	{
		return of(first.sinister(), first.dexter(), dusk);
	}

	public static <A, B, C> Triad<A, B, C> of(Dyad<A, Dyad<B, C>> dyad)
	{
		return of(dyad.sinister(), dyad.dexter().sinister(), dyad.dexter().dexter());
	}
}