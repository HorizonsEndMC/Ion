package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.player.CombatTimer.isNpcCombatTagged
import net.horizonsend.ion.server.features.player.CombatTimer.isPvpCombatTagged
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloting
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import org.bukkit.Bukkit

object VariableRenderDistance: IonServerComponent() {
	//Automatically changes every player's render distance based on circumstances every 3 seconds
	override fun onEnable() {
		Tasks.syncRepeat(0, 60) {
			updatePlayerRenderDistance()
		}
	}

	fun updatePlayerRenderDistance() {
		for (player in Bukkit.getOnlinePlayers()) {
			val renderDistance = when {
				// When dead
				player.isDead -> 4
				// When oped or in dutymode
				(player.isOp || player.hasPermission("group.dutymode")) -> 32
				// When on planet
				player.world.hasFlag(WorldFlag.PLANET_WORLD) -> 10
				// When in a creative server arena
				player.world.hasFlag(WorldFlag.ARENA) -> 22
				// When in combat but not piloting
				isPvpCombatTagged(player) && !isPiloting(player) -> 10
				//When in combat and piloting
				(isPvpCombatTagged(player) || isNpcCombatTagged(player)) && isPiloting(player) -> 22
				//Other cases, like when piloting but not in combat
				else -> 10
			}
			val simulationDistance = when {
				player.isDead -> 2
				isPvpCombatTagged(player) && !isPiloting(player) -> 4
				isPvpCombatTagged(player) || isNpcCombatTagged(player) && isPiloting(player) -> 5
				else -> 4
			}
			player.viewDistance = renderDistance
			player.simulationDistance = simulationDistance
		}
	}
}
