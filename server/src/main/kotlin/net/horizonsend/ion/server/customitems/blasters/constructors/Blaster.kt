package net.horizonsend.ion.server.customitems.blasters.constructors

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItem
import net.horizonsend.ion.server.utilities.getBukkitEquivalent
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.database.schema.misc.SLPlayer
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

abstract class Blaster : CustomItem() {

	protected fun getParticleType(entity: Entity) : Particle {
		return if (entity	is Player) {
			PlayerData[entity.uniqueId].chosenParticle.getBukkitEquivalent()
		} else Particle.REDSTONE
	}

	protected fun getParticleColour(entity: Entity) : Color{
		return if (entity is Player){
			val playerData = PlayerData[entity.uniqueId]
			val nation = SLPlayer[entity.uniqueId]?.nation
			if (playerData.chosenColour != null && playerData.patreonMoney >= IonServer.Ion.configuration.ParticleColourChoosingMoneyRequirement!!) {
				return Color.fromRGB(playerData.chosenColour!!.red, playerData.chosenColour!!.green, playerData.chosenColour!!.blue)
			}
			if (nation != null){
				return Color.fromRGB(NationCache[nation].color)
			}
			return Color.RED
		} else	Color.RED
	}
}