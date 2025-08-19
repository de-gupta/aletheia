package de.gupta.aletheia.forge;

import java.util.function.Predicate;

public final class OrderedPredicate<T> implements Predicate<T>, Comparable<OrderedPredicate<T>>
{
	private final int order;
	private final Predicate<T> predicate;

	public static <T> OrderedPredicate<T> of(final int order, final Predicate<T> predicate)
	{
		return new OrderedPredicate<>(order, predicate);
	}

	@Override
	public int compareTo(final OrderedPredicate<T> o)
	{
		return Integer.compare(this.order, o.order);
	}

	@Override
	public boolean test(final T t)
	{
		return this.predicate.test(t);
	}

	private OrderedPredicate(final int order, final Predicate<T> predicate)
	{
		this.order = order;
		this.predicate = predicate;
	}
}