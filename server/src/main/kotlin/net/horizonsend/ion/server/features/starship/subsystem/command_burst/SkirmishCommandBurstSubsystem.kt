package net.horizonsend.ion.server.features.starship.subsystem.command_burst

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.starship.SkirmishCommandBurstBalancing
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.SkirmishCommandBurstMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.Sign

class SkirmishCommandBurstSubsystem(
	starship: Starship,
	sign: Sign,
	multiblock: SkirmishCommandBurstMultiblock,
	) : AbstractCommandBurstSubsystem<SkirmishCommandBurstBalancing>(
	starship,
	sign,
	multiblock,
	starship.balancingManager.getCommandBurstSupplier(SkirmishCommandBurstSubsystem::class)
	) {
	override fun activateEffect(starships: Set<Starship>) {
		val playerPilot = starship.playerPilot ?: return
		val frontierNationId = PlayerCache[playerPilot].frontierNationOid

		if (frontierNationId == null) {
			playerPilot.userError("You must be in a nation to use command bursts!")
			return
		}

		starship.addStatusEffect(
			StarshipStatusEffect(
				StarshipStatusEffectTypes.CRUISE_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis
			)
		)

		starship.addStatusEffect(
			StarshipStatusEffect(
				StarshipStatusEffectTypes.DIRECT_CONTROL_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis
			)
		)

		for (otherStarship in starships) {
			val otherPlayer = otherStarship.playerPilot ?: continue
			if (otherPlayer == playerPilot) continue

			val otherNationId = PlayerCache[otherPlayer].frontierNationOid
			if (frontierNationId != otherNationId) continue

			otherStarship.addStatusEffect(StarshipStatusEffect(
				StarshipStatusEffectTypes.CRUISE_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis
			))

			otherStarship.addStatusEffect(StarshipStatusEffect(
				StarshipStatusEffectTypes.DIRECT_CONTROL_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis
			))
		}
	}

	override fun getName(): Component {
		return text("Skirmish Command Burst")
	}
}
