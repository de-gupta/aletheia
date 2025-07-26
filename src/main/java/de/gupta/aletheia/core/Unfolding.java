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

	private Unfolding(T value)
	{
		this.value = value;
	}

	public static <T> Unfolding<T> of(T value)
	{
		if (value == null)
		{
			return empty();
		}
		return new Unfolding<>(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> Unfolding<T> empty()
	{
		return (Unfolding<T>) EMPTY;
	}

	public T get()
	{
		if (value == null)
		{
			throw new IllegalStateException("Unfolding is empty");
		}
		return value;
	}

	public <R> Unfolding<R> map(Function<? super T, ? extends R> mapper)
	{
		Objects.requireNonNull(mapper, "mapper must not be null");
		return isEmpty() ? empty() : of(mapper.apply(value));
	}

	public Unfolding<T> mapIf(Predicate<? super T> predicate, Function<? super T, ? extends T> mapper)
	{
		Objects.requireNonNull(predicate, "predicate must not be null");
		Objects.requireNonNull(mapper, "mapper must not be null");

		return isEmpty() ? this : predicate.test(value) ? of(mapper.apply(value)) : this;
	}

	public <R> Unfolding<R> mapIfs(Predicate<? super T> predicate, Function<? super T, ? extends R> mapper)
	{
		Objects.requireNonNull(predicate, "predicate must not be null");
		Objects.requireNonNull(mapper, "mapper must not be null");

		return isEmpty() ? empty() : predicate.test(value) ? of(mapper.apply(value)) : empty();
	}

	public Unfolding<T> filter(Predicate<? super T> predicate)
	{
		Objects.requireNonNull(predicate, "predicate must not be null");
		return isEmpty() ? this : predicate.test(value) ? this : empty();
	}

	public Unfolding<T> tap(Consumer<? super T> consumer)
	{
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (isPresent()) consumer.accept(value);
		return this;
	}

	public <R> R with(Function<? super T, ? extends R> extractor)
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

	public boolean isPresent()
	{
		return !isEmpty();
	}

	public boolean isEmpty()
	{
		return this == EMPTY;
	}

	@Override
	public String toString()
	{
		return "Unfolding[" + value + "]";
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof final Unfolding<?> unfolding)) return false;
		if (isEmpty() && unfolding.isEmpty()) return true;
		return !isEmpty() && !unfolding.isEmpty() && Objects.equals(value, unfolding.value);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(value);
	}
}