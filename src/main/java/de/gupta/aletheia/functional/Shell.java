package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Dyad;

import java.util.*;
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
	public Unfolding<T> interdict(final Predicate<? super T> judgement,
	                              final Supplier<? extends RuntimeException> wrath)
	{
		return instance();
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
	public <U, R> Unfolding<Dyad<U, R>> metamorphose(final Function<? super T, ? extends U> fate,
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
	public <R> R cleave(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment)
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
	public <R> R smite(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
	                   final Supplier<? extends RuntimeException> wrath)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> R fulminate(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public Verdict<T> verdict()
	{
		return EmptyVerdict.instance();
	}

	@Override
	public <R> Unfolding<R> trifurcate(final ToIntFunction<? super T> reckoning,
	                                   final Function<? super T, ? extends R> diminished,
	                                   final Function<? super T, ? extends R> balanced,
	                                   final Function<? super T, ? extends R> ascendant)
	{
		return instance();
	}

	@Override
	public <R> R trifurcate(final ToIntFunction<? super T> reckoning,
	                        final Supplier<? extends R> diminished,
	                        final Supplier<? extends R> balanced,
	                        final Supplier<? extends R> ascendant)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> R trifurcate(final ToIntFunction<? super T> reckoning, final R diminished, final R balanced,
	                        final R ascendant)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<Dyad<T, R>> interlace(final Function<? super T, ? extends R> interlacing)
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
	public <U> Unfolding<T> wield(final Function<? super T, ? extends U> instrument,
	                              final BiFunction<Unfolding<T>, ? super U, Unfolding<T>> wielding)
	{
		return instance();
	}

	@Override
	public <U, R> Unfolding<R> sanctify(final Function<? super T, U> marriage,
										final BiFunction<? super T, ? super U, R> conjugation,
										final Predicate<? super R> judgement,
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
	public <R> R coronate(final Function<? super T, ? extends R> proclamation)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> proclamation, final R refuge)
	{
		Objects.requireNonNull(proclamation, "proclamation may not be null");
		Objects.requireNonNull(refuge, "refuge may not be null");
		return refuge;
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> proclamation, final Supplier<? extends R> refuge)
	{
		Objects.requireNonNull(proclamation, "proclamation may not be null");
		Objects.requireNonNull(refuge, "refuge may not be null");
		return refuge.get();
	}

	@Override
	public <R> R reap(final R harvest)
	{
		Objects.requireNonNull(harvest, "harvest may not be null");
		return harvest;
	}

	@Override
	public <R> R reap(final Supplier<? extends R> harvest)
	{
		Objects.requireNonNull(harvest, "harvest may not be null");
		return harvest.get();
	}

	@Override
	public <R, A> Unfolding<A> convoke(final SequencedCollection<Function<? super T, ? extends R>> omens,
	                                   final Function<? super SequencedCollection<? extends R>, ? extends A> oracle)
	{
		return instance();
	}

	@Override
	public <R> R coronate(final Predicate<? super T> judgement, final Function<? super T, ? extends R> reward,
						  final Function<? super T, ? extends R> punishment)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public T infuse(final Supplier<? extends T> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");

		return revelation.get();
	}

	@Override
	public T infuse(final T manifestation)
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
	public Unfolding<T> unlace(final Predicate<? super T> judgement, final Consumer<? super T> impregnator)
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