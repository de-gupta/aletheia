package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Brook confluent tests")
final class BrookConfluentTest
{
	@Nested
	@DisplayName("Aspects of tributary casting")
	final class TributaryCastingTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("tributaryShapes")
		@DisplayName("should preserve stream content through confluent")
		void shouldPreserveStreamContentThroughConfluent(final String as, final TributaryCase tc)
		{
			var confluent = Brook.confluent(tc.tributary().get());

			assertThat(confluent)
					.as("confluent stream instance should be forwarded for %s", as)
					.isSameAs(tc.expectedIdentity());

			assertThat(confluent.toList())
					.as("confluent contents should match for %s", as)
					.isEqualTo(tc.expectedElements());
		}

		private static Stream<Arguments> tributaryShapes()
		{
			return Stream.of(
					TributaryCase.from("String tributary remains untouched", List.of("a", "b", "c")),
					TributaryCase.from("Integer tributary remains untouched", List.of(4, 9, 16)),
					TributaryCase.from("Empty tributary remains untouched", List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record TributaryCase(String as, Supplier<Stream<?>> tributary,
		                             Stream<?> expectedIdentity, List<?> expectedElements)
		{
			private static <T> TributaryCase from(final String as, final List<T> values)
			{
				var stream = values.stream();
				return new TributaryCase(as, () -> stream, stream, values);
			}
		}
	}

	@Nested
	@DisplayName("Aspects of null tributary")
	final class NullTributaryTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("nullTributaryShapes")
		@DisplayName("should allow null tributary pass-through")
		void shouldAllowNullTributaryPassThrough(final String as, final NullTributaryCase tc)
		{
			assertThat(Brook.confluent(tc.tributary()))
					.as("confluent should return null unchanged for %s", as)
					.isNull();
		}

		private static Stream<Arguments> nullTributaryShapes()
		{
			return Stream.of(
					new NullTributaryCase("Nameless null stream", null)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record NullTributaryCase(String as, Stream<?> tributary)
		{
		}
	}
}