package de.gupta.aletheia.collection.folding;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface Loom<E>
{
	static <E> Loom<E> harness(Iterable<? extends E> iterable)
	{
		Objects.requireNonNull(iterable);
		return new Loom<>()
		{
			@Override
			public <R> R weave(R initial, BiFunction<R, ? super E, R> f)
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

	<R> R weave(R initial, BiFunction<R, ? super E, R> f);

	default E forge(BinaryOperator<E> op)
	{
		final Holder<E> holder = new Holder<>();
		weave(null, (_, e) ->
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
			throw new IllegalArgumentException("Empty loom");
		}

		return holder.value;
	}

	final class Holder<E>
	{
		E value;
		boolean isSet = false;
	}
}