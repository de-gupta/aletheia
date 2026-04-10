package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade infuse(collection) tests")
final class CascadeInfuseCollectionTest
{
	@Nested
	@DisplayName("Aspects of manifestation choice")
	final class ManifestationChoiceTests
	{
		@Test
		@DisplayName("should ignore manifestation for brook and return summon")
		void shouldIgnoreManifestationForBrookAndReturnSummon()
		{
			var result = Cascade.beckon("north", "south").infuse(List.of("x", "y"));

			assertThat(result).containsExactly("north", "south");
		}

		@Test
		@DisplayName("should return manifestation reference for abyss")
		void shouldReturnManifestationReferenceForAbyss()
		{
			var manifestation = new ArrayList<>(List.of("x", "y"));
			var result = Cascade.<String>abyss().infuse(manifestation);

			assertThat(result).isSameAs(manifestation);
		}
	}

	@Nested
	@DisplayName("Aspects of manifestation guard")
	final class ManifestationGuardTests
	{
		@Test
		@DisplayName("should reject null manifestation for brook")
		void shouldRejectNullManifestationForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).infuse((List<Integer>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("manifestation may not be null");
		}

		@Test
		@DisplayName("should reject null manifestation for abyss")
		void shouldRejectNullManifestationForAbyss()
		{
			assertThatThrownBy(() -> Cascade.<Integer>abyss().infuse((List<Integer>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("manifestation may not be null");
		}
	}
}
