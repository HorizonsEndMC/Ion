package net.horizonsend.ion.server.features.starship.subsystem.command_burst

import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.server.configuration.starship.ShieldCommandBurstBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.ShieldCommandBurstMultiblock
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.Sign

class ShieldCommandBurstSubsystem(
	starship: Starship,
	sign: Sign,
	multiblock: ShieldCommandBurstMultiblock,
	) : AbstractCommandBurstSubsystem<ShieldCommandBurstBalancing>(starship, sign, multiblock, starship.balancingManager.getCommandBurstSupplier(ShieldCommandBurstSubsystem::class
)) {
	override fun activate() {
		val loc = starship.centerOfMass.toLocation(starship.world)
		starship.addStatusEffect(StarshipStatusEffect(StarshipStatusEffectTypes.SHIELD_HEALTH_BOOST, 10000.0, 30L))
		toPlayersInRadius(loc, balancing.range) {
			it.hint("pluh")
		}
	}

	override fun getName(): Component {
		return text("Shield Command Burst")
	}
}
