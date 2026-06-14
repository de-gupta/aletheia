package de.gupta.aletheia.trials;

import java.util.function.Function;

@FunctionalInterface
public interface Ordeal<T, R>
{
	static <T, R> Ordeal<T, R> of(final Function<? super T, ? extends R> function)
	{
		return function::apply;
	}

	// ── Conventional names ────────────────────────────────────────────────────────────────────────

	default R apply(final T value) throws Exception
	{
		return endure(value);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────

	R endure(final T sacrifice) throws Exception;
}