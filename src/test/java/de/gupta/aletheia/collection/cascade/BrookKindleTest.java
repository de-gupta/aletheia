package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.crucible.Crucible;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Brook kindle tests")
final class BrookKindleTest
{
	// -------------------------------------------------------------------------
	// kindle(Supplier<Stream<E>>)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of kindle(Supplier<Stream>)")
	final class SupplierOverloadTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("supplierShapes")
		@DisplayName("should forge brook from stream supplier")
		void shouldForgeBrookFromStreamSupplier(final String as, final SupplierCase tc)
		{
			var result = Brook.kindle(tc.source());

			assertThat(result)
					.as("kindle(supplier) as %s should yield Brook", as)
					.isInstanceOf(Brook.class);
			assertThat(result.summon())
					.as("summon should render supplier stream for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("should invoke supplier freshly on each summon")
		void shouldInvokeSupplierFreshlyOnEachSummon()
		{
			var counter = new int[]{0};
			var cascade = Brook.kindle(() ->
			{
				counter[0]++;
				return Stream.of("omen");
			});

			cascade.summon();
			cascade.summon();

			assertThat(counter[0])
					.as("supplier should be invoked once per summon")
					.isEqualTo(2);
		}

		@Test
		@DisplayName("should reject null supplier")
		void shouldRejectNullSupplier()
		{
			assertThatThrownBy(() -> Brook.kindle((Supplier<Stream<Object>>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("source may not be null");
		}

		private static Stream<Arguments> supplierShapes()
		{
			return Stream.of(
					new SupplierCase("A stream with a singular omen",
							() -> Stream.of("echo"), List.of("echo")),
					new SupplierCase("A stream with many runes",
							() -> Stream.of(1, 2, 3), List.of(1, 2, 3)),
					new SupplierCase("A stream bearing null among offerings",
							() -> Stream.of("north", null, "south"), Arrays.asList("north", null, "south")),
					new SupplierCase("An empty stream remains a supple brook",
							Stream::empty, List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SupplierCase(String as, Supplier<Stream<Object>> source, List<Object> expected)
		{
		}
	}

	// -------------------------------------------------------------------------
	// kindle(Collection<? extends E>)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of kindle(Collection)")
	final class CollectionOverloadTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("collectionShapes")
		@DisplayName("should forge brook from collection")
		void shouldForgeBrookFromCollection(final String as, final CollectionCase tc)
		{
			var result = Brook.kindle(tc.source());

			assertThat(result)
					.as("kindle(collection) as %s should yield Brook", as)
					.isInstanceOf(Brook.class);
			assertThat(result.supple())
					.as("result should be supple for %s", as)
					.isTrue();
			assertThat(result.summon())
					.as("summon should preserve collection order for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("should reject null collection")
		void shouldRejectNullCollection()
		{
			assertThatThrownBy(() -> Brook.kindle((Collection<Object>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("elements may not be null");
		}

		private static Stream<Arguments> collectionShapes()
		{
			return Stream.of(
					new CollectionCase("A list of runes flows in sequence",
							List.of("odin", "thor", "loki"), List.of("odin", "thor", "loki")),
					new CollectionCase("A linked set keeps insertion order",
							new LinkedHashSet<>(List.of(3, 1, 2)), List.of(3, 1, 2)),
					new CollectionCase("Null elements are carried unchanged",
							Arrays.asList("atlas", null, "echo"), Arrays.asList("atlas", null, "echo")),
					new CollectionCase("An empty collection yields a supple empty brook",
							List.of(), List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record CollectionCase(String as, Collection<Object> source, List<Object> expected)
		{
		}
	}

	// -------------------------------------------------------------------------
	// kindle(E[])
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of kindle(array)")
	final class ArrayOverloadTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("arrayShapes")
		@DisplayName("should forge brook from array")
		void shouldForgeBrookFromArray(final String as, final ArrayCase tc)
		{
			var result = Brook.kindle(tc.source());

			assertThat(result)
					.as("kindle(array) as %s should yield Brook", as)
					.isInstanceOf(Brook.class);
			assertThat(result.supple())
					.as("result should be supple for %s", as)
					.isTrue();
			assertThat(result.summon())
					.as("summon should preserve array order for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("should reject null array")
		void shouldRejectNullArray()
		{
			assertThatThrownBy(() -> Brook.kindle((Object[]) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("elements may not be null");
		}

		private static Stream<Arguments> arrayShapes()
		{
			return Stream.of(
					new ArrayCase("A single omen starts the stream",
							new Object[]{"atlas"}, List.of("atlas")),
					new ArrayCase("Many runes flow in sequence",
							new Object[]{1, 2, 3}, List.of(1, 2, 3)),
					new ArrayCase("Null elements are carried unchanged",
							new Object[]{"oracle", null, "echo"}, Arrays.asList("oracle", null, "echo")),
					new ArrayCase("An empty array yields a supple empty brook",
							new Object[]{}, List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record ArrayCase(String as, Object[] source, List<Object> expected)
		{
		}
	}

	// -------------------------------------------------------------------------
	// kindle(Stream<? extends E>)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of kindle(Stream)")
	final class StreamOverloadTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("streamShapes")
		@DisplayName("should forge brook from stream")
		void shouldForgeBrookFromStream(final String as, final StreamCase tc)
		{
			var result = Brook.kindle(tc.source());

			assertThat(result)
					.as("kindle(stream) as %s should yield Brook", as)
					.isInstanceOf(Brook.class);
			assertThat(result.supple())
					.as("result should be supple for %s", as)
					.isTrue();
			assertThat(result.summon())
					.as("summon should contain stream elements for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("should accept a stream of subtypes")
		void shouldAcceptStreamOfSubtypes()
		{
			Stream<Integer> intStream = Stream.of(1, 2, 3);
			Cascade<Number> result = Brook.kindle(intStream);

			assertThat(result.summon()).containsExactly(1, 2, 3);
		}

		@Test
		@DisplayName("should reject null stream")
		void shouldRejectNullStream()
		{
			assertThatThrownBy(() -> Brook.kindle((Stream<Object>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("stream may not be null");
		}

		private static Stream<Arguments> streamShapes()
		{
			return Stream.of(
					new StreamCase("A stream with a single rune",
							Stream.of("zeus"), List.of("zeus")),
					new StreamCase("A stream with many gods",
							Stream.of("zeus", "hera", "ares"), List.of("zeus", "hera", "ares")),
					new StreamCase("An empty stream yields a supple empty brook",
							Stream.empty(), List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record StreamCase(String as, Stream<Object> source, List<Object> expected)
		{
		}
	}

	// -------------------------------------------------------------------------
	// kindle(Crucible<E>)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of kindle(Crucible)")
	final class CrucibleOverloadTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("crucibleShapes")
		@DisplayName("should forge brook from crucible manifestation")
		void shouldForgeBrookFromCrucibleManifestation(final String as, final CrucibleCase tc)
		{
			var result = Brook.kindle(tc.source());

			assertThat(result)
					.as("kindle(crucible) as %s should yield Brook", as)
					.isInstanceOf(Brook.class);
			assertThat(result.supple())
					.as("result should be supple for %s", as)
					.isTrue();
			assertThat(result.summon())
					.as("summon should mirror crucible manifestation for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("should read crucible manifestation lazily at each summon")
		void shouldReadCrucibleLazilyAtSummon()
		{
			var forge = Crucible.kindle(List.of("hephaestus"));
			var result = Brook.kindle(forge);

			assertThat(result.summon())
					.as("kindle(crucible) should read crucible manifestation at summon time")
					.containsExactly("hephaestus");
		}

		@Test
		@DisplayName("should reject null crucible")
		void shouldRejectNullCrucible()
		{
			assertThatThrownBy(() -> Brook.kindle((Crucible<Object>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("crucible may not be null");
		}

		private static Stream<Arguments> crucibleShapes()
		{
			return Stream.of(
					new CrucibleCase("A forge with several runes becomes brook",
							Crucible.kindle(List.of("iron", "silver", "gold")),
							List.of("iron", "silver", "gold")),
					new CrucibleCase("A relic also kindles into brook",
							Crucible.consecrate(List.of(7, 8, 9)),
							List.of(7, 8, 9)),
					new CrucibleCase("A crucible with null-bearing elements carries them",
							Crucible.kindle(Arrays.asList("sun", null, "moon")),
							Arrays.asList("sun", null, "moon")),
					new CrucibleCase("An empty crucible yields a supple empty brook",
							Crucible.kindle(List.of()),
							List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record CrucibleCase(String as, Crucible<Object> source, List<Object> expected)
		{
		}
	}
}