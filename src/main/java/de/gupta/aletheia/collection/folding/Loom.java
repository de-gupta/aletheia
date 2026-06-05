package de.gupta.aletheia.collection.folding;

import de.gupta.aletheia.functional.Unfolding;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface Loom<E>
{
	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard fold/reduce-style API. Each method delegates to its mythic equivalent below.

	static <E> Loom<E> of(final Iterable<? extends E> iterable)
	{
		return thread(iterable);
	}

	default <R> R fold(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation)
	{
		return weave(initial, operation);
	}

	default Unfolding<E> reduce(final BiFunction<? super E, ? super E, ? extends E> operation)
	{
		return forge(operation);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.

	static <E> Loom<E> thread(final Iterable<? extends E> iterable)
	{
		Objects.requireNonNull(iterable);
		return new Loom<>()
		{
			@Override
			public <R> R weave(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation)
			{
				R result = initial;
				for (E e : iterable)
				{
					result = operation.apply(result, e);
				}
				return result;
			}
		};
	}

	<R> R weave(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation);

	default Unfolding<E> forge(final BiFunction<? super E, ? super E, ? extends E> operation)
	{
		return weave(Unfolding.chaos(), (acc, e) ->
				acc.sterile() ? Unfolding.beckon(e) : acc.conjoin(e, operation));
	}
}