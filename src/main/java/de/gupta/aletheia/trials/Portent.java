package de.gupta.aletheia.trials;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Conventional (exception-recovery-style) factory is listed first for easy discovery.
 * Delegates to its canonical mythic counterpart below.
 */
public final class Portent<R>
{
	private final Class<? extends Exception> omen;
	private final Function<? super Exception, ? extends R> remedy;

	// ── Conventional names ────────────────────────────────────────────────────────────────────────

	/**
	 * @see #foretell(Class, Function)
	 */
	public static <E extends Exception, R> Portent<R> on(final Class<E> exceptionType,
	                                                     final Function<? super E, ? extends R> handler)
	{
		return foretell(exceptionType, handler);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────

	public static <E extends Exception, R> Portent<R> foretell(final Class<E> omen,
	                                                           final Function<? super E, ? extends R> remedy)
	{
		Objects.requireNonNull(omen, "omen may not be null");
		Objects.requireNonNull(remedy, "remedy may not be null");

		return new Portent<>(omen, fury -> remedy.apply(omen.cast(fury)));
	}

	Optional<R> heed(final Exception fury)
	{
		Objects.requireNonNull(fury, "fury may not be null");

		return omen.isInstance(fury) ? Optional.of(remedy.apply(fury)) : Optional.empty();
	}

	private Portent(final Class<? extends Exception> omen, final Function<? super Exception, ? extends R> remedy)
	{
		this.omen = omen;
		this.remedy = remedy;
	}
}