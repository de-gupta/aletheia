package de.gupta.aletheia.forge;

import java.util.function.Predicate;

public final class OrderedPredicate<T> implements Predicate<T>, Comparable<OrderedPredicate<T>>
{
	private final Ordered<Predicate<T>> delegate;

	public static <T> OrderedPredicate<T> of(final int order, final Predicate<T> predicate)
	{
		return new OrderedPredicate<>(order, predicate);
	}

	@Override
	public boolean test(final T t)
	{
		return this.delegate.get().test(t);
	}

	@Override
	public int compareTo(final OrderedPredicate<T> o)
	{
		return this.delegate.compareTo(o.delegate);
	}

	private OrderedPredicate(final int order, final Predicate<T> predicate)
	{
		this.delegate = Ordered.of(order, predicate);
	}
}