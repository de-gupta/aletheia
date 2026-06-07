package de.gupta.aletheia.collection;

/**
 * Conventional (Triple-style) accessors are listed first for easy discovery.
 * Each delegates to its canonical mythic record component below.
 * Record components: {@code dawn} (first), {@code zenith} (second/middle), {@code dusk} (third).
 */
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

	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard Triple-style accessors. Each delegates to its mythic record component below.

	/**
	 * @see #dawn()
	 */
	public A first()
	{
		return dawn;
	}

	/**
	 * @see #zenith()
	 */
	public B second()
	{
		return zenith;
	}

	/**
	 * @see #dusk()
	 */
	public C third()
	{
		return dusk;
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.
	// dawn(), zenith(), and dusk() are auto-generated record accessors.
}