package de.gupta.aletheia.forge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class OrderedTest
{
	@Test
	@DisplayName("Ordered should compare by order sinister")
	void shouldCompareByOrderFirst()
	{
		Ordered<String> first = Ordered.of(1, "a");
		Ordered<String> second = Ordered.of(2, "b");

		assertThat(first).isLessThan(second);
		assertThat(second).isGreaterThan(first);
	}

	@Test
	@DisplayName("Ordered should compare by sequence if orders are equal")
	void shouldCompareBySequenceIfOrdersAreEqual()
	{
		Ordered<String> first = Ordered.of(1, "a");
		Ordered<String> second = Ordered.of(1, "b");

		assertThat(first).isLessThan(second);
		assertThat(second).isGreaterThan(first);
	}

	@Test
	@DisplayName("Ordered should implement equals and hashCode correctly")
	void shouldImplementEqualsAndHashCodeCorrectly()
	{
		Ordered<String> first = Ordered.of(1, "a");
		Ordered<String> second = Ordered.of(1, "a");
		Ordered<String> third = Ordered.of(2, "a");
		Ordered<String> fourth = Ordered.of(1, "b");

		// We cannot easily test sequence equality without reflection or knowing the order of creation,
		// but we can at least test that different objects are not equal.
		assertThat(first).isEqualTo(first);
		assertThat(first).isNotEqualTo(null);
		assertThat(first).isNotEqualTo("not a wrapper");
		assertThat(first).isNotEqualTo(second); // Different sequence
		assertThat(first).isNotEqualTo(third); // Different order
		assertThat(first).isNotEqualTo(fourth); // Different element

		assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
	}

	@Test
	@DisplayName("hashCode() computes the expected formula using order, sequence, and element")
	void hashCodeFollowsFormula() throws Exception
	{
		final Ordered<String> o = Ordered.of(2, "test");

		final Field sequenceField = Ordered.class.getDeclaredField("sequence");
		sequenceField.setAccessible(true);
		final long sequence = (long) sequenceField.get(o);

		int expected = 2;
		expected = 31 * expected + Long.hashCode(sequence);
		expected = 31 * expected + "test".hashCode();

		assertThat(o.hashCode()).isEqualTo(expected);
	}

	@Test
	@DisplayName("get() returns the wrapped element")
	void shouldReturnWrappedElement()
	{
		assertThat(Ordered.of(0, "hermes").get()).isEqualTo("hermes");
		assertThat(Ordered.of(1, 42).get()).isEqualTo(42);
	}

	@Test
	@DisplayName("get() returns null when the wrapped element is null")
	void shouldReturnNullWhenElementIsNull()
	{
		assertThat(Ordered.of(0, (String) null).get()).isNull();
	}

	@Test
	@DisplayName("hashCode() uses 0 as element contribution when element is null")
	void hashCodeWithNullElement() throws Exception
	{
		final Ordered<String> o = Ordered.of(0, null);

		final Field sequenceField = Ordered.class.getDeclaredField("sequence");
		sequenceField.setAccessible(true);
		final long sequence = (long) sequenceField.get(o);

		int expected = 0;
		expected = 31 * expected + Long.hashCode(sequence);
		expected = 31 * expected + 0;  // null element → 0

		assertThat(o.hashCode()).isEqualTo(expected);
	}

	@Test
	@DisplayName("equals() returns true when all fields match (via reflected sequence alignment)")
	void equalsReturnsTrueForIdenticalFields() throws Exception
	{
		final Ordered<String> a = Ordered.of(1, "hades");
		final Ordered<String> b = Ordered.of(1, "hades");

		final Field sequenceField = Ordered.class.getDeclaredField("sequence");
		sequenceField.setAccessible(true);
		sequenceField.set(b, sequenceField.get(a));

		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}
}