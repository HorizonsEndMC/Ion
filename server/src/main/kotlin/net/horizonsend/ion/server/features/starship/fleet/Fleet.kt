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
import org.bukkit.entity.Player
import java.util.Locale
import java.util.UUID

class Fleet(var leaderId: UUID) : ForwardingAudience {

    private val memberIds = mutableSetOf(leaderId)
    private val invitedIds = mutableSetOf<UUID>()
    var lastBroadcast = ""

    private fun add(playerId: UUID) {
        memberIds.add(playerId)
    }

    fun add(player: Player) = add(player.uniqueId)

    private fun remove(playerId: UUID) {
        memberIds.remove(playerId)

        // removed player is the leader
        if (leaderId == playerId && memberIds.isNotEmpty()) {
            // if the first player id is not valid, delete the fleet (there is likely an error)
            if (!switchLeader(memberIds.first())) {
                delete()
            }
        }
    }

    fun remove(player: Player) = remove(player.uniqueId)

    private fun get(playerId: UUID) = memberIds.contains(playerId)

    fun get(player: Player) = get(player.uniqueId)

    private fun invite(playerId: UUID) {
        invitedIds.add(playerId)
    }

    fun invite(player: Player) = invite(player.uniqueId)

    private fun removeInvite(playerId: UUID) {
        if (invitedIds.contains(playerId)) invitedIds.remove(playerId)
    }

    fun removeInvite(player: Player) = removeInvite(player.uniqueId)

    private fun getInvite(playerId: UUID) = invitedIds.contains(playerId)

    fun getInvite(player: Player) = getInvite(player.uniqueId)

    fun delete() {
        memberIds.clear()
        invitedIds.clear()
    }

    private fun switchLeader(newLeaderId: UUID): Boolean {
        val player = Bukkit.getPlayer(newLeaderId) ?: return false

        leaderId = newLeaderId
        for (memberId in memberIds) {
            val member = Bukkit.getPlayer(memberId) ?: continue
            member.information("${player.name} is now the new Fleet Commander of your fleet")
        }

        return true
    }

    fun switchLeader(player: Player): Boolean = switchLeader(player.uniqueId)

    fun broadcast(broadcast: String) {
        lastBroadcast = broadcast

        for (memberId in memberIds) {
            val player = Bukkit.getPlayer(memberId) ?: continue
            player.showTitle(Title.title(text("Fleet Broadcast", HE_DARK_ORANGE), text(broadcast), Title.DEFAULT_TIMES))
        }
    }

    fun jumpFleet() {
        for (memberId in memberIds) {
            val player = Bukkit.getPlayer(memberId) ?: continue

            MiscStarshipCommands.onJump(player)
        }
    }

    fun jumpFleet(x: Int, z: Int) {
        for (memberId in memberIds) {
            val player = Bukkit.getPlayer(memberId) ?: continue

            MiscStarshipCommands.onJump(player, x.toString(), z.toString(), null)
        }
    }

    fun jumpFleet(destination: String) {
        for (memberId in memberIds) {
            val player = Bukkit.getPlayer(memberId) ?: continue

            MiscStarshipCommands.onJump(player, destination, null)
        }
    }

    fun list(player: Player) {
        player.sendMessage(lineBreakWithCenterText(ofChildren(
            text(FLEET_ICON.text, HE_LIGHT_ORANGE).font(Sidebar.fontKey),
            space(),
            text("Fleet Members", HE_LIGHT_ORANGE)))
        )
        player.sendMessage(ofChildren(
            text(FLEET_COMMANDER_ICON.text, GOLD).font(Sidebar.fontKey),
            space(),
            text("Fleet Commander", HE_MEDIUM_GRAY),
            text(": ", HE_DARK_GRAY),
            text(Bukkit.getPlayer(leaderId)?.name ?: "None")
        ))

        player.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(47))
        for (memberId in memberIds) {
            val memberPlayer = Bukkit.getPlayer(memberId) ?: continue
            val starship = PilotedStarships[memberPlayer]

            val line = template(
                "{0} piloting {1} {2} in {3}",
                color = HE_LIGHT_GRAY,
                paramColor = WHITE,
                useQuotesAroundObjects = true,
                memberPlayer.name,
                starship?.getDisplayName() ?: "nothing",
                bracketed(text(starship?.initialBlockCount ?: 0, WHITE)),
                memberPlayer.location.world.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            )
            player.sendMessage(line)
        }
    }

    override fun audiences() : Iterable<Audience> = memberIds.mapNotNull(Bukkit::getPlayer)
}