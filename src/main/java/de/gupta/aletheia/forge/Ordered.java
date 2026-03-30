package de.gupta.aletheia.forge;

import de.gupta.aletheia.functional.Unfolding;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Ordered<T> implements Supplier<T>, Comparable<Ordered<T>>
{
	private static final AtomicLong SEQUENCER = new AtomicLong();

	private final int order;
	private final long sequence;
	private final T element;

	public static <T> Ordered<T> of(final int order, final T element)
	{
		return new Ordered<>(order, element);
	}

	@Override
	public T get()
	{
		return this.element;
	}

	@Override
	public int compareTo(final Ordered<T> o)
	{
		return Unfolding.beckon(Integer.compare(this.order, o.order))
						.coronate(c -> c != 0, Function.identity(), _ -> Long.compare(this.sequence, o.sequence));
	}

	@Override
	public int hashCode()
	{
		int result = order;
		result = 31 * result + Long.hashCode(sequence);
		result = 31 * result + (element != null ? element.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(final Object o)
	{
		return Unfolding.beckon(o)
						.discern(obj -> this == obj)
						.supple()
				|| Unfolding.beckon(o)
							.discern(obj -> obj != null && this.getClass() == obj.getClass())
							.metamorphose(obj -> (Ordered<?>) obj)
							.discern(that -> this.order == that.order &&
									this.sequence == that.sequence &&
									Objects.equals(this.element, that.element))
							.supple();
	}

	private Ordered(final int order, final T element)
	{
		this.order = order;
		this.sequence = SEQUENCER.getAndIncrement();
		this.element = element;
	}
}