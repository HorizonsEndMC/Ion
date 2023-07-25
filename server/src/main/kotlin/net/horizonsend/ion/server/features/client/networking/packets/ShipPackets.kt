package net.horizonsend.ion.server.features.client.networking.packets

import net.horizonsend.ion.server.features.client.networking.IonPacketHandler
import net.horizonsend.ion.server.features.client.networking.Packets
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.FriendlyByteBuf
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.roundToHundredth
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.math.roundToInt

object ShipData : IonPacketHandler() {
	override val name = "ship_data"

	fun enable() = Tasks.asyncRepeat(
		delay = 10L,
		interval = 10L
	) {
		Bukkit.getOnlinePlayers().forEach { player ->
			val ship = ActiveStarships.findByPilot(player) ?: run {
				Packets.SHIP_DATA.send(player)
				return@forEach
			}

			val name = MiniMessage.miniMessage().deserialize(ship.data.name ?: ship.type.formatted)
			val type = MiniMessage.miniMessage().deserialize(ship.type.formatted)
			val pm = ship.reactor.powerDistributor
			val targets = ship.autoTurretTargets.mapValues { Bukkit.getOfflinePlayer(it.value).name ?: "None" }
			val hull = ship.hullIntegrity().times(100).roundToInt()
			val gravwell = ship.isInterdicting
			val weaponset = ship.weaponSetSelections[player.uniqueId] ?: "None"
			val regenEfficiency = ship.shieldEfficiency

			val targetSpeed = ship.cruiseData.targetSpeed
			val speed = ship.cruiseData.velocity.length().roundToHundredth()

			Packets.SHIP_DATA.send(
				player,

				name,
				type,
				targets, // Targets
				hull, // Hull integrity
				gravwell, // is welling
				weaponset, // Chosen weaponset
				regenEfficiency, // Shield regen efficiency
				targetSpeed,
				speed,
				pm.shieldPortion, pm.weaponPortion, pm.thrusterPortion // PowerModes
			)
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun s2c(buf: FriendlyByteBuf, player: Player, vararg arguments: Any) {
		if (ActiveStarships.findByPilot(player) == null) {
			buf.writeBoolean(false)
			return
		} else buf.writeBoolean(true)

		val name = arguments[0] as Component
		val type = arguments[1] as Component
		val targets = arguments[2] as Map<String, String>
		val hull = arguments[3] as Int
		val gravwell = arguments[4] as Boolean
		val weaponset = arguments[5] as String
		val regenEfficiency = arguments[6] as Double

		val targetSpeed = arguments[7] as Int
		val speed = arguments[8] as Double

		val shield = arguments[9] as Double
		val weapon = arguments[10] as Double
		val thruster = arguments[11] as Double

		buf.writeComponent(name)
		buf.writeComponent(type)

		buf.writeInt(targets.size)
		targets.forEach {
			buf.writeUtf(it.key)
			buf.writeUtf(it.value)
		}

		buf.writeInt(hull)
		buf.writeBoolean(gravwell)
		buf.writeUtf(weaponset)
		buf.writeDouble(regenEfficiency)

		buf.writeInt(targetSpeed)
		buf.writeDouble(speed)

		buf.writeDouble(shield)
		buf.writeDouble(weapon)
		buf.writeDouble(thruster)
	}
}
