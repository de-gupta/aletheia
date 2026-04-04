package de.gupta.aletheia.collection;

public record Triplet<A, B, C>(A first, B second, C third)
{
	public static <A, B, C> Triplet<A, B, C> of(A first, B second, C third)
	{
		return new Triplet<>(first, second, third);
	}

	public static <A, B, C> Triplet<A, B, C> of(A first, Pair<B, C> second)
	{
		return new Triplet<>(first, second.first(), second.second());
	}

	public static <A, B, C> Triplet<A, B, C> of(Pair<A, B> first, C third)
	{
		return new Triplet<>(first.first(), first.second(), third);
	}

	public static <A, B, C> Triplet<A, B, C> of(Pair<A, Pair<B, C>> pair)
	{
		return new Triplet<>(pair.first(), pair.second().first(), pair.second().second());
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