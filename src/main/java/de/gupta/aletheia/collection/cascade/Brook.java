package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.Dyad;
import de.gupta.aletheia.collection.crucible.Crucible;
import de.gupta.aletheia.collection.crucible.Forge;
import de.gupta.aletheia.collection.crucible.Relic;
import de.gupta.aletheia.collection.folding.Loom;
import de.gupta.aletheia.functional.Unfolding;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Brook<E> implements Cascade<E>
{
	private final Supplier<Stream<E>> source;

	static <E> Cascade<E> kindle(final Supplier<Stream<E>> source)
	{
		Objects.requireNonNull(source, "source may not be null");
		return ignite(source);
	}

	static <E> Cascade<E> kindle(final Collection<? extends E> elements)
	{
		Objects.requireNonNull(elements, "elements may not be null");
		return ignite(() -> confluent(elements.stream()));
	}

	static <E> Cascade<E> kindle(final E[] elements)
	{
		Objects.requireNonNull(elements, "elements may not be null");
		return ignite(() -> Arrays.stream(elements));
	}

	static <E> Cascade<E> kindle(final Stream<? extends E> stream)
	{
		Objects.requireNonNull(stream, "stream may not be null");
		return ignite(() -> confluent(stream));
	}

	static <E> Cascade<E> kindle(final Crucible<E> crucible)
	{
		Objects.requireNonNull(crucible, "crucible may not be null");
		return ignite(() -> crucible.manifest().stream());
	}

	private static <E> Brook<E> ignite(final Supplier<Stream<E>> source)
	{
		return new Brook<>(source);
	}

	@SuppressWarnings("unchecked")
	static <E> Stream<E> confluent(final Stream<? extends E> tributary)
	{
		return (Stream<E>) tributary;
	}

	@Override
	public boolean sterile()
	{
		return false;
	}

	@Override
	public boolean supple()
	{
		return true;
	}

	@Override
	public Collection<E> summon()
	{
		return Collections.unmodifiableList(new ArrayList<>(materialise()));
	}

	/**
	 * Presence and extraction
	 */

	@Override
	public Stream<E> stream()
	{
		return source.get();
	}

	@Override
	public Relic<E> enshrine()
	{
		return Crucible.consecrate(materialise());
	}

	@Override
	public Forge<E> awaken()
	{
		return Crucible.kindle(materialise());
	}

	@Override
	public <R> R coronate(final Function<? super Stream<E>, ? extends R> conclusion)
	{
		Objects.requireNonNull(conclusion, "conclusion may not be null");
		return conclusion.apply(source.get());
	}

	@Override
	public <R> R coronate(final Function<? super Stream<E>, ? extends R> conclusion, final Supplier<? extends R> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");
		return coronate(conclusion);
	}

	@Override
	public <F> Cascade<F> metamorphose(final Function<? super E, ? extends F> metamorphosis)
	{
		Objects.requireNonNull(metamorphosis, "metamorphosis may not be null");
		return transmute(s -> confluent(s.map(metamorphosis).filter(Objects::nonNull)));
	}

	@Override
	public <U, F> Cascade<Dyad<U, F>> metamorphose(final Function<? super E, ? extends U> fate,
	                                               final Function<? super E, ? extends F> destiny)
	{
		Objects.requireNonNull(fate, "fate may not be null");
		Objects.requireNonNull(destiny, "destiny may not be null");
		return transmute(s -> s.map(e -> Dyad.of(fate.apply(e), destiny.apply(e))));
	}

	@Override
	public Cascade<E> develop(final Predicate<? super E> judgement,
	                          final Function<? super E, ? extends E> development)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(development, "development may not be null");
		return channel(s -> s.map(e -> Unfolding.beckon(e)
		                                        .develop(judgement, development)
		                                        .infuse(e))
		                     .filter(Objects::nonNull));
	}

	/**
	 * Transformation and transmutation
	 */

	@Override
	public <F> Cascade<F> evolve(final Predicate<? super E> judgement,
	                             final Function<? super E, ? extends F> evolution)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(evolution, "evolution may not be null");
		return transmute(s -> confluent(s.filter(judgement).map(evolution).filter(Objects::nonNull)));
	}

	@Override
	public Cascade<E> ascend(final UnaryOperator<E> ascension, final int levels)
	{
		Objects.requireNonNull(ascension, "ascension may not be null");
		return channel(s -> s.map(e -> Unfolding.beckon(e)
		                                        .ascend(ascension, levels)
		                                        .infuse(e))
		                     .filter(Objects::nonNull));
	}

	@Override
	public <F> Cascade<Dyad<E, F>> interlace(final Function<? super E, ? extends F> interlacing)
	{
		Objects.requireNonNull(interlacing, "interlacing may not be null");
		return transmute(s -> s.map(e -> Dyad.of(e, interlacing.apply(e))));
	}

	@Override
	public Cascade<E> purify()
	{
		return channel(Stream::distinct);
	}

	@Override
	public Cascade<E> ordain(final Comparator<? super E> order)
	{
		Objects.requireNonNull(order, "order may not be null");
		return channel(s -> s.sorted(order));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Cascade<E> ordain()
	{
		return channel(s -> s.sorted((a, b) -> ((Comparable<E>) a).compareTo(b)));
	}

	@Override
	public Cascade<E> admit(final E element)
	{
		Objects.requireNonNull(element, "element may not be null");
		return channel(s -> Stream.concat(s, Stream.of(element)));
	}

	@Override
	public Cascade<E> precede(final E element)
	{
		Objects.requireNonNull(element, "element may not be null");
		return channel(s -> Stream.concat(Stream.of(element), s));
	}

	@Override
	public <F> Cascade<F> transfigure(final Function<Collection<? extends E>, ? extends Collection<F>> transmutation)
	{
		Objects.requireNonNull(transmutation, "transmutation may not be null");

		return Unfolding.beckon(transmutation.apply(source.get().toList()))
		                .metamorphose(Cascade::beckon)
		                .infuse(Cascade::abyss);
	}

	@Override
	public Cascade<E> discern(final Predicate<? super E> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return channel(s -> s.filter(judgement));
	}

	@Override
	public Cascade<E> discern(final Predicate<? super E> judgement,
	                          final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(wrath, "wrath may not be null");
		return channel(s -> s.map(e -> Unfolding.beckon(e).discern(judgement, wrath).summon()));
	}

	@Override
	public <F> Cascade<F> cleave(final Predicate<? super E> judgement,
	                             final Function<? super E, ? extends F> reward,
	                             final Function<? super E, ? extends F> punishment)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");
		return transmute(s -> confluent(s.map(e -> Unfolding.beckon(e)
		                                                    .cleave(judgement, reward, punishment)
		                                                    .summon())
		                                 .filter(Objects::nonNull)));
	}

	/**
	 * Judgements and branching
	 */

	@Override
	public <F> Cascade<F> entwine(final Function<? super E, Cascade<F>> plot)
	{
		Objects.requireNonNull(plot, "plot may not be null");
		return transmute(s -> s.flatMap(e -> Unfolding.beckon(plot.apply(e))
		                                              .metamorphose(Cascade::stream)
		                                              .infuse(Stream::empty)));
	}

	@Override
	public <F> Cascade<F> alchemize(final Function<? super E, Optional<? extends F>> potion)
	{
		Objects.requireNonNull(potion, "potion may not be null");
		return transmute(s -> s.flatMap(e -> confluent(potion.apply(e).stream())));
	}

	@Override
	public <U, F> Cascade<F> conjoin(final U consort,
	                                 final BiFunction<? super E, ? super U, ? extends F> conjugation)
	{
		Objects.requireNonNull(consort, "consort may not be null");
		Objects.requireNonNull(conjugation, "conjugation may not be null");
		return transmute(s -> confluent(s.map(e -> conjugation.apply(e, consort)).filter(Objects::nonNull)));
	}

	/**
	 * Composition
	 */

	@Override
	public <F, G> Cascade<G> braid(final Cascade<F> consort,
	                               final BiFunction<Unfolding<E>, Unfolding<F>, ? extends G> weaver)
	{
		Objects.requireNonNull(consort, "consort may not be null");
		Objects.requireNonNull(weaver, "weaver may not be null");

		return transmute(s ->
		{
			var left = s.toList();
			var right = consort.stream().toList();
			int size = Math.max(left.size(), right.size());
			var zipped = new ArrayList<G>(size);
			for (int i = 0; i < size; i++)
			{
				var l = i < left.size() ? Unfolding.beckon(left.get(i)) : Unfolding.<E>chaos();
				var r = i < right.size() ? Unfolding.beckon(right.get(i)) : Unfolding.<F>chaos();
				zipped.add(weaver.apply(l, r));
			}
			return zipped.stream();
		});
	}

	@Override
	public <R> R weave(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation)
	{
		Objects.requireNonNull(operation, "operation may not be null");
		return Loom.thread(materialise()).weave(initial, operation);
	}

	@Override
	public Unfolding<E> herald()
	{
		return Unfolding.distill(source.get());
	}

	@Override
	public Cascade<E> forsake(final int n)
	{
		return channel(s -> s.skip(n));
	}

	@Override
	public Cascade<E> temper(final int n)
	{
		return channel(s -> s.limit(n));
	}

	@Override
	public <R, A> R precipitate(final Collector<? super E, A, R> collector)
	{
		Objects.requireNonNull(collector, "collector may not be null");
		return source.get().collect(collector);
	}

	@Override
	public long reckon()
	{
		return source.get().count();
	}

	@Override
	public Unfolding<E> zenith(final Comparator<? super E> comparator)
	{
		Objects.requireNonNull(comparator, "comparator may not be null");
		return Unfolding.augur(source.get().max(comparator));
	}

	@Override
	public Unfolding<E> nadir(final Comparator<? super E> comparator)
	{
		Objects.requireNonNull(comparator, "comparator may not be null");
		return Unfolding.augur(source.get().min(comparator));
	}

	@Override
	public boolean affirm(final Predicate<? super E> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return source.get().allMatch(judgement);
	}

	@Override
	public boolean permit(final Predicate<? super E> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return source.get().anyMatch(judgement);
	}

	@Override
	public boolean deny(final Predicate<? super E> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return source.get().noneMatch(judgement);
	}

	@Override
	public boolean harbor(final E element)
	{
		return source.get().anyMatch(e -> Objects.equals(e, element));
	}

	@Override
	public Unfolding<E> seek(final Predicate<? super E> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return Unfolding.distill(source.get().filter(judgement));
	}

	@Override
	public Unfolding<E> dusk()
	{
		return Unfolding.augur(source.get().reduce((_, b) -> b));
	}

	@Override
	public <K> Cascade<E> amalgamate(final Function<? super E, ? extends K> essence,
	                                 final BinaryOperator<E> confluence)
	{
		Objects.requireNonNull(essence, "essence may not be null");
		Objects.requireNonNull(confluence, "confluence may not be null");

		return Cascade.beckon(
				source.get()
				      .collect(Collectors.toMap(essence, Function.identity(), confluence, LinkedHashMap::new))
				      .values());
	}

	@Override
	public <K> Cascade<E> amalgamate(final Function<? super E, ? extends K> essence,
	                                 final BinaryOperator<E> confluence,
	                                 final Predicate<? super E> dissolution)
	{
		Objects.requireNonNull(essence, "essence may not be null");
		Objects.requireNonNull(confluence, "confluence may not be null");
		Objects.requireNonNull(dissolution, "dissolution may not be null");

		return amalgamate(essence, confluence).discern(dissolution.negate());
	}

	@Override
	public Unfolding<E> smelt(final BiFunction<? super E, ? super E, ? extends E> operation)
	{
		Objects.requireNonNull(operation, "operation may not be null");
		return Loom.thread(materialise()).forge(operation);
	}

	@Override
	public Collection<E> infuse(final Supplier<? extends Collection<? extends E>> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return summon();
	}

	/**
	 * Folding / reduction
	 */

	@Override
	public Collection<E> infuse(final Collection<? extends E> manifestation)
	{
		Objects.requireNonNull(manifestation, "manifestation may not be null");
		return summon();
	}

	@Override
	public Cascade<E> resurrect(final Supplier<Cascade<E>> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");
		return this;
	}

	/**
	 * Recovery and renewal
	 */

	@Override
	public Cascade<E> revive(final Supplier<? extends E> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");
		return this;
	}

	@Override
	public Cascade<E> unlace(final Consumer<? super E> impregnator)
	{
		Objects.requireNonNull(impregnator, "impregnator may not be null");
		return channel(s -> s.peek(impregnator));
	}

	@Override
	public Cascade<E> unlace(final Predicate<? super E> judgement, final Consumer<? super E> impregnator)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(impregnator, "impregnator may not be null");
		return channel(s -> s.peek(e -> Unfolding.beckon(e).unlace(judgement, impregnator)));
	}

	@Override
	public String toString()
	{
		return "Cascade[...]";
	}

	/**
	 * Effects
	 */

	private Brook<E> channel(final UnaryOperator<Stream<E>> course)
	{
		return Brook.ignite(() -> course.apply(source.get()));
	}

	private <F> Brook<F> transmute(final Function<Stream<E>, Stream<F>> alchemy)
	{
		return Brook.ignite(() -> alchemy.apply(source.get()));
	}

	private List<E> materialise()
	{
		return source.get().toList();
	}

	private Brook(final Supplier<Stream<E>> source)
	{
		this.source = source;
	}
}