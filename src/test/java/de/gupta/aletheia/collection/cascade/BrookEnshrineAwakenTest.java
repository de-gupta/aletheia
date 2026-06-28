package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.crucible.Forge;
import de.gupta.aletheia.collection.crucible.Relic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Brook — enshrine(), awaken(), toString()")
final class BrookEnshrineAwakenTest
{
	@Test
	@DisplayName("enshrine() returns a Relic whose manifest mirrors the brook's elements")
	void enshrineReturnsRelicWithElements()
	{
		Relic<String> relic = Brook.kindle(List.of("athena", "ares", "apollo")).enshrine();
		assertThat(relic.manifest()).containsExactly("athena", "ares", "apollo");
	}

	@Test
	@DisplayName("enshrine() on an empty brook yields an empty Relic")
	void enshrineOnEmptyBrookYieldsEmptyRelic()
	{
		Relic<String> relic = Brook.kindle(List.<String>of()).enshrine();
		assertThat(relic.manifest()).isEmpty();
	}

	@Test
	@DisplayName("awaken() returns a Forge whose manifest mirrors the brook's elements")
	void awakenReturnsForgeWithElements()
	{
		Forge<String> forge = Brook.kindle(List.of("zeus", "hera", "poseidon")).awaken();
		assertThat(forge.manifest()).containsExactly("zeus", "hera", "poseidon");
	}

	@Test
	@DisplayName("awaken() on an empty brook yields an empty Forge")
	void awakenOnEmptyBrookYieldsEmptyForge()
	{
		Forge<String> forge = Brook.kindle(List.<String>of()).awaken();
		assertThat(forge.manifest()).isEmpty();
	}

	@Test
	@DisplayName("toString() returns the sentinel string")
	void toStringReturnsSentinel()
	{
		assertThat(Brook.kindle(List.of("x")).toString()).isEqualTo("Cascade[...]");
	}
}
