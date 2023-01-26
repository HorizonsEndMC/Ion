package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.extensions.sendServerError
import net.horizonsend.ion.server.items.CustomItems.customItem
import net.horizonsend.ion.server.items.objects.Blaster
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import net.horizonsend.ion.server.vaultEconomy
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.feature.progression.Levels
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.System.currentTimeMillis
import java.util.UUID
import kotlin.math.roundToInt

@Suppress("Unused")
class PlayerDeathListener : Listener {
	private val coolDowns = mutableMapOf<UUID, Long>()

	@EventHandler
	@Suppress("Unused")
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val victim = event.player

		// Skulls start
		val killer = event.entity.killer

		killer?.let killer@{ killerNotNull ->
			// Custom death message start
			killerNotNull.inventory.itemInMainHand.customItem?.let blaster@{ customItem ->
				if (customItem !is Blaster<*>) return@blaster

				val blaster = customItem.displayName

				val victimColor = "<#" + Integer.toHexString((SLPlayer[victim.uniqueId]?.nation?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"
				val killerColor = "<#" + Integer.toHexString((SLPlayer[killerNotNull.uniqueId]?.nation?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"

				val distance = killerNotNull.location.distance(victim.location)

				val verb = when (customItem.identifier) {
					"SNIPER" -> "sniped"
					"SHOTGUN" -> "blasted"
					"RIFLE" -> "shot"
					"SUBMACHINE_BLASTER" -> "shredded"
					"PISTOL" -> "pelted"
					else -> "shot"
				}

				val newMessage = MiniMessage.miniMessage()
					.deserialize(
						"$victimColor${victim.name}<reset> was $verb by $killerColor${killerNotNull.name}<reset> from ${distance.roundToInt()} blocks away, using "
					)
					.append(blaster)

				event.deathMessage(newMessage)
			}

			// Bounties start
			let bounties@{
				if (killerNotNull !== victim) killerNotNull.rewardAchievement(Achievement.KILL_PLAYER) // Kill a Player Achievement

				val killerData = PlayerData[killerNotNull.uniqueId]
				val killerLevel = Levels[event.player]
				val victimData = PlayerData[victim.uniqueId]

				killerData.update {
					bounty += (killerLevel * killerLevel) + (200 * killerLevel) + 5000

					if (killerData.acceptedBounty == victim.uniqueId) {
						if (vaultEconomy != null) {
							val bounty = victimData.bounty

							vaultEconomy.depositPlayer(victim, bounty.toDouble())

							acceptedBounty = null

							victimData.update {
								this.bounty = 0
							}

							killerNotNull.sendRichMessage("<gray>Claimed </gray>$bounty<gray> bounty on </gray>${victimData.minecraftUsername}")
						} else {
							killerNotNull.sendServerError("Vault Economy is not loaded! Cannot reward bounty!")
						}
					}
				}
			}
			// Bounties end
		}

		// Player Head Drops
		let heads@{
			val headDropCooldownEnd = coolDowns.getOrDefault(victim.uniqueId, 0)
			coolDowns[victim.uniqueId] = currentTimeMillis() + 1000 * 60 * 60
			if (headDropCooldownEnd > currentTimeMillis()) return@heads

			val head = ItemStack(Material.PLAYER_HEAD)
			head.editMeta(SkullMeta::class.java) {
				it.owningPlayer = victim
			}

			event.entity.world.dropItem(victim.location, head)
		}

		// Skulls end
	}
}
