package net.horizonsend.ion.server.features.starship.subsystem.command_burst

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.starship.CapitalSkirmishCommandBurstBalancing
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.CapitalSkirmishCommandBurstMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.block.Sign

class CapitalSkirmishCommandBurstSubsystem(
	starship: Starship,
	sign: Sign,
	multiblock: CapitalSkirmishCommandBurstMultiblock,
	) : AbstractCommandBurstSubsystem<CapitalSkirmishCommandBurstBalancing>(
	starship,
	sign,
	multiblock,
	starship.balancingManager.getCommandBurstSupplier(CapitalSkirmishCommandBurstSubsystem::class)
	) {

	override val color: Color = Color.YELLOW

	override fun activateEffect(starships: Set<Starship>) {
		val playerPilot = starship.playerPilot ?: return
		val fleet = Fleets.findByMember(playerPilot)

		if (fleet == null) {
			playerPilot.userError("You must be in a fleet to use command bursts!")
			return
		}

		starship.addStatusEffect(
			StarshipStatusEffect(
				StarshipStatusEffectTypes.CRUISE_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis,
				starship,
			)
		)

		starship.addStatusEffect(
			StarshipStatusEffect(
				StarshipStatusEffectTypes.DIRECT_CONTROL_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis,
				starship,
			)
		)

		for (otherStarship in starships) {
			val otherPlayer = otherStarship.playerPilot ?: continue
			if (otherPlayer == playerPilot) continue

			val otherFleet = Fleets.findByMember(otherPlayer)
			if (fleet != otherFleet) continue

			otherStarship.addStatusEffect(StarshipStatusEffect(
				StarshipStatusEffectTypes.CRUISE_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis,
				starship,
			))

			otherStarship.addStatusEffect(StarshipStatusEffect(
				StarshipStatusEffectTypes.DIRECT_CONTROL_SPEED,
				balancing.effectStrength,
				balancing.effectDurationMillis,
				starship,
			))
		}
	}

	override fun getName(): Component {
		return text("Capital Skirmish Command Burst")
	}
}
