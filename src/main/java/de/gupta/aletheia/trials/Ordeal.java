package de.gupta.aletheia.trials;

import java.util.function.Function;

/**
 * Conventional (CheckedFunction-style) method is listed first for easy discovery.
 * Delegates to its canonical mythic counterpart below.
 */
@FunctionalInterface
public interface Ordeal<T, R>
{
	static <T, R> Ordeal<T, R> of(final Function<? super T, ? extends R> function)
	{
		return function::apply;
	}

	// ── Conventional names ────────────────────────────────────────────────────────────────────────

	/**
	 * @see #endure(Object)
	 */
	default R apply(final T value) throws Exception
	{
		return endure(value);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────

	R endure(final T sacrifice) throws Exception;
}