package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.BookmarkCache
import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import java.util.Locale

@CommandAlias("bookmark|bm")
object BookmarkCommand : SLCommand() {
    override fun onEnable(manager: PaperCommandManager) {
        registerAsyncCompletion(manager, "bookmarks") { c ->
            val player = c.player ?: throw InvalidCommandArgument("Players only")
            val slPlayerId = player.slPlayerId
            Bookmark.col.find(Bookmark::owner eq slPlayerId).map { it.name }.toList()
        }
    }

    private fun getMaxBookmarks(): Int {
        return 50
    }

    @Subcommand("save")
    @Suppress("unused")
    @CommandCompletion("name")
    fun onSave(sender: Player, @Optional name: String?) {
        if (!name.isNullOrBlank()) {
            if (name != name.lowercase(Locale.getDefault())) {
                sender.userError("Name must be lowercase")
                return
            }
            if (name.length !in 2..50) {
                sender.userError("Name length is ${name.length}, must be between 2 and 50")
                return
            }
            if (!name.replace('-', ' ').replace('_', ' ').isAlphanumeric(includeSpaces = true)) {
                sender.userError("Name must onlt contain letters, numbers, and - or _")
                return
            }
        }

        val loc = Vec3i(sender.location)
        val slPlayerId = sender.slPlayerId
        val worldName = sender.location.world.name
        val newName = if (name.isNullOrBlank()) {
            "${worldName}-${loc.x}-${loc.z}"
        } else name
        val exists = !Bookmark.none(and(Bookmark::owner eq sender.slPlayerId, Bookmark::name eq newName))

        if (!exists) {
            if (Bookmark.count(Bookmark::owner eq slPlayerId) > getMaxBookmarks()) {
                sender.userError("You can only have up to ${getMaxBookmarks()} bookmarks")
                return
            }

            Bookmark.create(newName, slPlayerId, loc, IonServer.configuration.serverName ?: "Survival", worldName)
            sender.success("Saved bookmark $newName")
        } else {
            sender.userError("Bookmark with name $newName already exists")
            return
        }
    }

    @Subcommand("delete")
    @Suppress("unused")
    @CommandCompletion("@bookmarks")
    fun onDelete(sender: Player, name: String) {
        val bookmark = Bookmark.find(and(Bookmark::owner eq sender.slPlayerId, Bookmark::name eq name)).first()
        if (bookmark == null) {
            sender.userError("You don't have a bookmark named $name")
            return
        }
        Bookmark.delete(bookmark._id)
        sender.success("Deleted bookmark ${bookmark.name}")
    }

    @Subcommand("list")
    @Suppress("unused")
    fun onCacheList(sender: Player) {
        val bookmarks: List<Bookmark> = getBookmarks(sender).sortedByDescending { it.name }

        if (bookmarks.isEmpty()) {
            sender.userError("You have no bookmarks")
            return
        }

        sender.sendMessage(lineBreakWithCenterText(Component.text("Active Starships", HEColorScheme.HE_LIGHT_ORANGE)))

        for (bookmark in bookmarks) {
            sender.information("${bookmark.name}: ${bookmark.worldName}, ${bookmark.x}, ${bookmark.y}, ${bookmark.z}")

            val line = template(
                "{0}: World {1] @ {2}, {3}, {4}",
                color = HE_LIGHT_GRAY,
                paramColor = WHITE,
                useQuotesAroundObjects = true,
                bookmark.name,
                bookmark.worldName,
                bookmark.x,
                bookmark.y,
                bookmark.z
            )

            sender.sendMessage(line)
        }

        sender.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(47))
    }

    fun getBookmarks(player: Player): List<Bookmark> {
        return BookmarkCache.getAll().filter { bm -> bm.owner == player.slPlayerId }
    }
}