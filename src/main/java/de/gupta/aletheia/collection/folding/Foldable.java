package de.gupta.aletheia.collection.folding;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface Foldable<E>
{
	static <E> Foldable<E> fromIterable(Iterable<? extends E> iterable)
	{
		Objects.requireNonNull(iterable);
		return new Foldable<>()
		{
			@Override
			public <R> R foldLeft(R initial, BiFunction<R, ? super E, R> f)
			{
				R result = initial;
				for (E e : iterable)
				{
					result = f.apply(result, e);
				}
				return result;
			}
		};
	}

	<R> R foldLeft(R initial, BiFunction<R, ? super E, R> f);

	default E reduceLeft(BinaryOperator<E> op)
	{
		final Holder<E> holder = new Holder<>();
		foldLeft(null, (acc, e) ->
		{
			if (!holder.isSet)
			{
				holder.value = e;
				holder.isSet = true;
			}
			else
			{
				holder.value = op.apply(holder.value, e);
			}
			return null;
		});

		if (!holder.isSet)
		{
			throw new IllegalArgumentException("Empty foldable");
		}

		return holder.value;
	}

	final class Holder<E>
	{
		E value;
		boolean isSet = false;
	}
}