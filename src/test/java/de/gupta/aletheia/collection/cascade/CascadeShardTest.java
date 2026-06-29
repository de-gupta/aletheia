package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#shard")
final class CascadeShardTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("shardsIntoGroupsCases")
		@DisplayName("partitions elements into consecutive groups of the given size")
		void shardsIntoGroups(final String as, final ShardCase tc)
		{
			assertThat(tc.source().shard(tc.size()).summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("last shard is smaller when cascade size is not evenly divisible")
		void lastShardIsSmallerWhenNotEvenlyDivisible()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5).shard(2).summon())
					.as("5 elements sharded by 2 → [1,2], [3,4], [5]")
					.containsExactly(List.of(1, 2), List.of(3, 4), List.of(5));
		}

		@Test
		@DisplayName("shard size 1 — each element is its own group")
		void shardSizeOneEachElementAlone()
		{
			assertThat(Cascade.beckon(1, 2, 3).shard(1).summon())
					.as("size 1 — each element in its own shard")
					.containsExactly(List.of(1), List.of(2), List.of(3));
		}

		@Test
		@DisplayName("shard size equals cascade size — single group containing all elements")
		void shardSizeEqualsCascadeSize()
		{
			assertThat(Cascade.beckon(1, 2, 3).shard(3).summon())
					.as("shard size = cascade size → single shard with all elements")
					.containsExactly(List.of(1, 2, 3));
		}

		@Test
		@DisplayName("shard size larger than cascade — single group containing all elements")
		void shardSizeLargerThanCascade()
		{
			assertThat(Cascade.beckon(1, 2).shard(10).summon())
					.as("shard size > cascade size → single shard with all elements")
					.containsExactly(List.of(1, 2));
		}

		@Test
		@DisplayName("encounter order is preserved within and across shards")
		void encounterOrderPreserved()
		{
			final var result = Cascade.beckon("a", "b", "c", "d", "e", "f").shard(2).summon().stream().toList();

			assertThat(result).containsExactly(List.of("a", "b"), List.of("c", "d"), List.of("e", "f"));
		}

		@Test
		@DisplayName("chunk() delegates to shard()")
		void chunkDelegatesToShard()
		{
			final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4);

			assertThat(source.chunk(2).summon())
					.as("chunk() must equal shard()")
					.containsExactlyElementsOf(source.shard(2).summon());
		}

		@Test
		@DisplayName("throws when size is zero")
		void throwsWhenSizeIsZero()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).shard(0))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("throws when size is negative")
		void throwsWhenSizeIsNegative()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).shard(-1))
					.isInstanceOf(IllegalArgumentException.class);
		}

		private static Stream<Arguments> shardsIntoGroupsCases()
		{
			return Stream.of(
					new ShardCase("6 elements by 3 — two equal shards",
							Cascade.beckon(1, 2, 3, 4, 5, 6), 3,
							List.of(List.of(1, 2, 3), List.of(4, 5, 6))),
					new ShardCase("4 elements by 2 — two equal shards",
							Cascade.beckon(10, 20, 30, 40), 2,
							List.of(List.of(10, 20), List.of(30, 40))),
					new ShardCase("single element by 1 — one shard",
							Cascade.beckon(42), 1,
							List.of(List.of(42)))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record ShardCase(String as, Cascade<Integer> source, int size, List<List<Integer>> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns absent Cascade")
		void returnsAbsent()
		{
			assertThat(Cascade.<Integer>abyss().shard(3).sterile())
					.as("shard on absent cascade stays absent")
					.isTrue();
		}
	}
}