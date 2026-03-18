package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.*;
import java.util.stream.Stream;

final class Shell<T> implements Unfolding<T>
{
	private static final Shell<?> INSTANCE = new Shell<>();

	@Override
	public T summon()
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public T decree(final Supplier<? extends RuntimeException> wrath)
	{
		throw wrath.get();
	}

	@Override
	public void interdict(final Supplier<? extends RuntimeException> wrath)
	{
		// do nothing
	}

	@Override
	public void interdict(final Function<? super T, Supplier<? extends RuntimeException>> wrath)
	{
		// do nothing
	}

	@Override
	public boolean sterile()
	{
		return true;
	}

	@Override
	public boolean supple()
	{
		return false;
	}

	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement)
	{
		return instance();
	}

	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement,
								final Supplier<? extends RuntimeException> wrath)
	{
		throw wrath.get();
	}

	@Override
	public Unfolding<T> develop(final Predicate<? super T> judgement,
								final Function<? super T, ? extends T> development)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis)
	{
		return instance();
	}

	@Override
	public <U, R> Unfolding<Pair<U, R>> metamorphose(final Function<? super T, ? extends U> fate,
													 final Function<? super T, ? extends R> destiny)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis,
										 final Supplier<? extends RuntimeException> wrath)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<R> alchemize(final Function<? super T, Optional<? extends R>> potion)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<R> evolve(final Predicate<? super T> judgement,
								   final Function<? super T, ? extends R> evolution)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<R> cleave(final Predicate<? super T> judgement, final Function<? super T, ? extends R> reward,
								   final Function<? super T, ? extends R> punishment)
	{
		return instance();
	}

	@Override
	public <R> R cleave(final Predicate<? super T> judgement, final R reward, final R punishment)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> R cleave(final Map<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> R smite(final Map<Predicate<? super T>, Function<? super T, R>> judgments,
					   final Supplier<? extends RuntimeException> wrath)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> R cleave(final SortedMap<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<Pair<T, R>> interlace(final Function<? super T, ? extends R> interlacing)
	{
		return instance();
	}

	@Override
	public <U, R> Unfolding<R> conjoin(final U consort, final BiFunction<? super T, ? super U, ? extends R> conjugation)
	{
		return instance();
	}

	@Override
	public <U, R> Unfolding<R> emanate(final Function<? super T, U> marriage,
									   final BiFunction<? super T, ? super U, R> conjugation)
	{
		return instance();
	}

	@Override
	public <U, R> Unfolding<R> sanctify(final Function<? super T, U> marriage,
										final BiFunction<? super T, ? super U, R> conjugation,
										final Predicate<? super R> judgment,
										final Supplier<? extends RuntimeException> wrath)
	{
		throw wrath.get();
	}

	@Override
	public <R, U> Unfolding<U> braid(final Unfolding<R> consort,
									 final BiFunction<? super T, ? super R, ? extends U> weaver)
	{
		return instance();
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> conclusion)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public T rescue(final Supplier<? extends T> revelation)
	{
		return revelation.get();
	}

	@Override
	public T rescue(final T manifestation)
	{
		return manifestation;
	}

	@Override
	public Unfolding<T> resurrect(final Supplier<Unfolding<T>> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");

		return Optional.ofNullable(grace.get()).orElseGet(Shell::instance);
	}

	@Override
	public Unfolding<T> resurrectWithValue(final Supplier<T> grace)
	{
		return revive(grace);
	}

	@Override
	public Unfolding<T> revive(final Supplier<T> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");

		return Unfolding.beckon(grace.get());
	}

	@Override
	public Stream<T> stream()
	{
		return Stream.empty();
	}

	@Override
	public Unfolding<T> unlace(final Consumer<? super T> impregnator)
	{
		return instance();
	}

	@Override
	public Optional<T> optional()
	{
		return Optional.empty();
	}

	public static <T> Unfolding<T> instance()
	{
		return (Unfolding<T>) INSTANCE;
	}

	@Override
	public String toString()
	{
		return "Unfolding{}";
	}

	@Override
	public Unfolding<T> ascend(final UnaryOperator<T> ascension, final int levels)
	{
		return instance();
	}

	private Shell()
	{
	}
}