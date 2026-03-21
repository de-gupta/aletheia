package de.gupta.aletheia.forge;

import java.util.function.Predicate;

public final class OrderedPredicate<T> extends OrderWrapper<Predicate<T>> implements Predicate<T>
{
	public static <T> OrderedPredicate<T> of(final int order, final Predicate<T> predicate)
	{
		return new OrderedPredicate<>(order, predicate);
	}

	@Override
	public boolean test(final T t)
	{
		return this.get().test(t);
	}

	private OrderedPredicate(final int order, final Predicate<T> predicate)
	{
		super(order, predicate);
	}
}