package de.gupta.aletheia.collection;

public record Triplet<A, B, C>(A first, B second, C third)
{
	public static <A, B, C> Triplet<A, B, C> of(A first, B second, C third)
	{
		return new Triplet<>(first, second, third);
	}

	public static <A, B, C> Triplet<A, B, C> of(A first, Dyad<B, C> second)
	{
		return of(first, second.sinister(), second.dexter());
	}

	public static <A, B, C> Triplet<A, B, C> of(Dyad<A, B> first, C third)
	{
		return of(first.sinister(), first.dexter(), third);
	}

	public static <A, B, C> Triplet<A, B, C> of(Dyad<A, Dyad<B, C>> dyad)
	{
		return of(dyad.sinister(), dyad.dexter().sinister(), dyad.dexter().dexter());
	}

	public A left()
	{
		return first;
	}

	public B center()
	{
		return second;
	}

	public C right()
	{
		return third;
	}
}