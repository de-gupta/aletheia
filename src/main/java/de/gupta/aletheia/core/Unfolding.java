package de.gupta.aletheia.core;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Unfolding<T>
{
	private static final Unfolding<?> EMPTY = new Unfolding<>(null);

	private final T value;

	public T reveal()
	{
		if (value == null)
		{
			throw new IllegalStateException("Unfolding is empty");
		}
		return value;
	}

	public <R> Unfolding<R> refold(Function<? super T, ? extends R> mapper)
	{
		Objects.requireNonNull(mapper, "mapper must not be null");
		return isEmpty() ? empty() : of(mapper.apply(value));
	}

	public boolean isEmpty()
	{
		return this == EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static <T> Unfolding<T> empty()
	{
		return (Unfolding<T>) EMPTY;
	}

	public static <T> Unfolding<T> of(T value)
	{
		if (value == null)
		{
			return empty();
		}
		return new Unfolding<>(value);
	}

	public Unfolding<T> develop(Predicate<? super T> predicate, Function<? super T, ? extends T> mapper)
	{
		Objects.requireNonNull(predicate, "predicate must not be null");
		Objects.requireNonNull(mapper, "mapper must not be null");

		return isEmpty() ? this : predicate.test(value) ? of(mapper.apply(value)) : this;
	}

	public <R> Unfolding<R> evolve(Predicate<? super T> predicate, Function<? super T, ? extends R> mapper)
	{
		Objects.requireNonNull(predicate, "predicate must not be null");
		Objects.requireNonNull(mapper, "mapper must not be null");

		return isEmpty() ? empty() : predicate.test(value) ? of(mapper.apply(value)) : empty();
	}

	public Unfolding<T> discern(Predicate<? super T> predicate)
	{
		Objects.requireNonNull(predicate, "predicate must not be null");
		return isEmpty() ? this : predicate.test(value) ? this : empty();
	}

	public Unfolding<T> unlace(Consumer<? super T> consumer)
	{
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (isPresent()) consumer.accept(value);
		return this;
	}

	public boolean isPresent()
	{
		return !isEmpty();
	}

	public <R> R concludeWith(Function<? super T, ? extends R> extractor)
	{
		Objects.requireNonNull(extractor, "extractor must not be null");
		if (isEmpty()) throw new IllegalStateException("Unfolding is empty");
		return extractor.apply(value);
	}

	public T alternatively(Supplier<? extends T> fallback)
	{
		Objects.requireNonNull(fallback, "fallback supplier must not be null");
		return isPresent() ? value : fallback.get();
	}

	public T alternatively(T fallback)
	{
		return isPresent() ? value : fallback;
	}

	public Optional<T> optional()
	{
		return Optional.ofNullable(value);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(value);
	}

	@Override
	public boolean equals(final Object o)
	{
		return this == o ||
				(o instanceof Unfolding<?> other &&
						(
								isEmpty() && other.isEmpty() ||
										(isPresent() && other.isPresent() && Objects.equals(value, other.value))
						)
				);
	}

	@Override
	public String toString()
	{
		return "Unfolding[" + value + "]";
	}

	private Unfolding(T value)
	{
		this.value = value;
	}
}