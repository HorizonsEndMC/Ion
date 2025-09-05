package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.FLEET_COMMANDER_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.FLEET_ICON
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class Fleet(var leader: FleetMember?, var initalized : Boolean = true) : ForwardingAudience {
	val createdAt = System.currentTimeMillis()

    val members = mutableSetOf<FleetMember>()
    private val invited = mutableSetOf<FleetMember>()
    var lastBroadcast = ""
	var logic: FleetLogic? = null

	init {
		leader?.let { members.add(it) }
	}

	fun add(member: FleetMember) {
		members.add(member)
		invited.remove(member)
	}

	fun remove(member: FleetMember, newLeader : ((Fleet) -> FleetMember?)? = null) {
		members.remove(member)
		if (leader == member) {
			leader = if (newLeader == null) null else newLeader(this)
		}
	}

    fun contains(player: Player) = members.any { member -> member is FleetMember.PlayerMember && member.uuid == player.uniqueId }

	fun invite(member: FleetMember) = invited.add(member)

	fun removeInvite(member: FleetMember) = invited.remove(member)

	fun isInvited(member: FleetMember) = invited.contains(member)

	fun isMember(member: FleetMember) = members.contains(member)

	fun playerMembers() = members.filterIsInstance<FleetMember.PlayerMember>().map { it.uuid }

	fun delete() {
		members.clear()
		invited.clear()
		leader = null
	}

	fun switchLeader(newLeader: FleetMember): Boolean {
		if (!members.contains(newLeader)) return false
		leader = newLeader
		return true
	}

    fun switchLeader(player: Player): Boolean {
		val result = switchLeader(player.toFleetMember())
		if (!result) return false

		for (memberId in members) {
			val member = (memberId as? FleetMember.PlayerMember)?.let { Bukkit.getPlayer(it.uuid) } ?: continue
			member.information("${player.name} is now the new Fleet Commander of your fleet")
		}
		return true
	}

    fun broadcast(broadcast: String) {
        lastBroadcast = broadcast

        for (memberId in members) {
            val player = (memberId as? FleetMember.PlayerMember)?.let { Bukkit.getPlayer(it.uuid) } ?: continue
            player.showTitle(Title.title(text("Fleet Broadcast", HE_DARK_ORANGE), text(broadcast), Title.DEFAULT_TIMES))
        }
    }

    fun jumpFleet() {
        for (memberId in members) {
            val player = (memberId as? FleetMember.PlayerMember)?.let { Bukkit.getPlayer(it.uuid) } ?: continue

            MiscStarshipCommands.onJump(player)
        }
    }

    fun jumpFleet(x: Int, z: Int) {
        for (memberId in members) {
            val player = (memberId as? FleetMember.PlayerMember)?.let { Bukkit.getPlayer(it.uuid) } ?: continue

            MiscStarshipCommands.onJump(player, x.toString(), z.toString(), null)
        }
    }

    fun jumpFleet(destination: String) {
        for (memberId in members) {
            val player = (memberId as? FleetMember.PlayerMember)?.let { Bukkit.getPlayer(it.uuid) } ?: continue

            MiscStarshipCommands.onJump(player, destination, null)
        }
    }

	fun useBeaconFleet() {
        for (member in members.filterIsInstance<FleetMember.PlayerMember>()) {
            val player = Bukkit.getPlayer(member.uuid) ?: continue

            MiscStarshipCommands.onUseBeacon(player)
        }
    }

    fun list(viewer: Player) {
		viewer.sendMessage(lineBreakWithCenterText(ofChildren(
			text(FLEET_ICON.text, HE_LIGHT_ORANGE).font(Sidebar.fontKey),
			space(),
			text("Fleet Members", HE_LIGHT_ORANGE)))
		)

		val leaderName : String = when (leader) {
			is FleetMember.PlayerMember -> Bukkit.getPlayer((leader as FleetMember.PlayerMember).uuid)?.name ?: "Offline Player"
			is FleetMember.AIShipMember -> (leader as FleetMember.AIShipMember).shipRef.get()?.getDisplayName()?.plainText() ?: "AI Ship"
			else -> "Unknown"
		}

		viewer.sendMessage(ofChildren(
			text(FLEET_COMMANDER_ICON.text, GOLD).font(Sidebar.fontKey),
			space(),
			text("Fleet Commander", HE_MEDIUM_GRAY),
			text(": ", HE_DARK_GRAY),
			text(leaderName)
		))

		viewer.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(47))

		for (member in members) {
			val line = when (member) {
				is FleetMember.PlayerMember -> {
					val player = Bukkit.getPlayer(member.uuid)
					if (player != null) {
						val starship = PilotedStarships[player]
						template(
							"{0} piloting {1} {2} in {3}",
							color = HE_LIGHT_GRAY,
							paramColor = WHITE,
							useQuotesAroundObjects = true,
							player.name,
							starship?.getDisplayName() ?: "nothing",
							bracketed(text(starship?.initialBlockCount ?: 0, WHITE)),
							player.location.world.name
						)
					} else {
						text("Offline Player (${member.name})", HE_MEDIUM_GRAY)
					}
				}

				is FleetMember.AIShipMember -> {
					val ship = member.shipRef.get()
					if (ship != null) {
						template(
							"AI Ship {0} with {1} blocks in {2}",
							color = HE_LIGHT_GRAY,
							paramColor = WHITE,
							useQuotesAroundObjects = true,
							ship.getDisplayName(),
							bracketed(text(ship.initialBlockCount, WHITE)),
							ship.world.name
						)
					} else {
						text("Destroyed AI Ship", HE_DARK_GRAY)
					}
				}
			}
			viewer.sendMessage(line)
		}
	}

	fun hasValidLeader(): Boolean {
		val leader = this.leader ?: return false
		return when (leader) {
			is FleetMember.PlayerMember -> Bukkit.getPlayer(leader.uuid) != null
			is FleetMember.AIShipMember -> leader.shipRef.get() != null
		}
	}

	fun getLeaderLocation(): Location? {
		val leader = leader ?: return null

		return when (leader) {
			is FleetMember.PlayerMember -> {
				val player = Bukkit.getPlayer(leader.uuid)
				player?.location
			}

			is FleetMember.AIShipMember -> {
				val ship = leader.shipRef.get()
				ship?.centerOfMass?.toLocation(ship.world)
			}
		}
	}


	override fun audiences(): Iterable<Audience> = members.mapNotNull {
		when (it) {
			is FleetMember.PlayerMember -> Bukkit.getPlayer(it.uuid)
			is FleetMember.AIShipMember -> null // AI ships aren't audiences
		}
	}

	companion object {
		fun largestAIShip(fleet : Fleet) : FleetMember? {
			return fleet.members.filter { (it as? FleetMember.AIShipMember)?.shipRef?.get() != null}
				.maxByOrNull {(it as FleetMember.AIShipMember).shipRef.get()!!.initialBlockCount}
		}

		fun firstPlayer(fleet: Fleet) : FleetMember? {
			return fleet.members.firstOrNull { (it as? FleetMember.PlayerMember) != null }
		}
	}
}
