package de.gupta.aletheia.core;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

	public <R> Unfolding<R> refold(Function<? super T, ? extends R> folding)
	{
		Objects.requireNonNull(folding, "Folding must not be null");
		return isEmpty() ? empty() : of(folding.apply(value));
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

	public Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development)
	{
		Objects.requireNonNull(judgement, "judgement must not be null");
		Objects.requireNonNull(development, "development must not be null");

		return isEmpty() ? this : judgement.test(value) ? of(development.apply(value)) : this;
	}

	public <R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution)
	{
		Objects.requireNonNull(judgement, "judgement must not be null");
		Objects.requireNonNull(evolution, "evolution must not be null");

		return isEmpty() ? empty() : judgement.test(value) ? of(evolution.apply(value)) : empty();
	}

	public Stream<T> stream()
	{
		return isEmpty() ? Stream.empty() : Stream.of(value);
	}

	public <R> Unfolding<R> cleave(final Predicate<? super T> judgement,
								   final Function<? super T, ? extends R> reward,
								   final Function<? super T, ? extends R> punishment)
	{
		Objects.requireNonNull(judgement, "judgement must not be null");
		Objects.requireNonNull(reward, "reward must not be null");
		Objects.requireNonNull(punishment, "punishment must not be null");

		return isEmpty() ? empty() : judgement.test(value) ? of(reward.apply(value)) : of(punishment.apply(value));
	}

	public Unfolding<T> discern(Predicate<? super T> judgement)
	{
		Objects.requireNonNull(judgement, "judgement must not be null");
		return isEmpty() ? this : judgement.test(value) ? this : empty();
	}

	public Unfolding<T> unlace(Consumer<? super T> impregnator)
	{
		Objects.requireNonNull(impregnator, "impregnator must not be null");
		if (isPresent()) impregnator.accept(value);
		return this;
	}

	public boolean isPresent()
	{
		return !isEmpty();
	}

	public <R> R concludeWith(Function<? super T, ? extends R> conclusion)
	{
		Objects.requireNonNull(conclusion, "conclusion must not be null");
		if (isEmpty()) throw new IllegalStateException("Unfolding is empty");
		return conclusion.apply(value);
	}

	public T alternatively(Supplier<? extends T> revelation)
	{
		Objects.requireNonNull(revelation, "revelation must not be null");
		return isPresent() ? value : revelation.get();
	}

	public T alternatively(T alternate)
	{
		return isPresent() ? value : alternate;
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