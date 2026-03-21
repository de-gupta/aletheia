package de.gupta.aletheia.forge;

import de.gupta.aletheia.functional.Unfolding;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

public final class OrderedPredicate<T> implements Predicate<T>, Comparable<OrderedPredicate<T>>
{
	private static final AtomicLong SEQUENCER = new AtomicLong();

	private final int order;
	private final long sequence;
	private final Predicate<T> predicate;

	public static <T> OrderedPredicate<T> of(final int order, final Predicate<T> predicate)
	{
		return new OrderedPredicate<>(order, predicate);
	}

	@Override
	public int compareTo(final OrderedPredicate<T> o)
	{
		return Unfolding.beckon(Integer.compare(this.order, o.order))
						.coronate(c -> c != 0, Function.identity(), _ -> Long.compare(this.sequence, o.sequence));
	}

	@Override
	public boolean test(final T t)
	{
		return this.predicate.test(t);
	}

	private OrderedPredicate(final int order, final Predicate<T> predicate)
	{
		this.order = order;
		this.sequence = SEQUENCER.getAndIncrement();
		this.predicate = predicate;
	}
}