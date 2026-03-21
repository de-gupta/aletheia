package de.gupta.aletheia.forge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderWrapperTest
{
	@Test
	@DisplayName("OrderWrapper should compare by order first")
	void shouldCompareByOrderFirst()
	{
		OrderWrapper<String> first = OrderWrapper.of(1, "a");
		OrderWrapper<String> second = OrderWrapper.of(2, "b");

		assertThat(first).isLessThan(second);
		assertThat(second).isGreaterThan(first);
	}

	@Test
	@DisplayName("OrderWrapper should compare by sequence if orders are equal")
	void shouldCompareBySequenceIfOrdersAreEqual()
	{
		OrderWrapper<String> first = OrderWrapper.of(1, "a");
		OrderWrapper<String> second = OrderWrapper.of(1, "b");

		assertThat(first).isLessThan(second);
		assertThat(second).isGreaterThan(first);
	}

	@Test
	@DisplayName("OrderWrapper should implement equals and hashCode correctly")
	void shouldImplementEqualsAndHashCodeCorrectly()
	{
		OrderWrapper<String> first = OrderWrapper.of(1, "a");
		OrderWrapper<String> second = OrderWrapper.of(1, "a");
		OrderWrapper<String> third = OrderWrapper.of(2, "a");
		OrderWrapper<String> fourth = OrderWrapper.of(1, "b");

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
}