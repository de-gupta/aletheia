package de.gupta.aletheia.trials;

import java.util.function.Function;

@FunctionalInterface
public interface Ordeal<T, R>
{
	static <T, R> Ordeal<T, R> of(final Function<? super T, ? extends R> function)
	{
		return function::apply;
	}

	R endure(final T sacrifice) throws Exception;
}