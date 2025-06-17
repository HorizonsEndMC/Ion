package net.horizonsend.ion.server.command.admin

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.UniversalNPC
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.economy.CityNpcCommand.getIdFromName
import net.horizonsend.ion.server.features.npcs.database.DatabaseNPCs
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.type.DatabaseNPCType
import net.horizonsend.ion.server.features.npcs.database.type.DatabaseNPCTypes
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import org.bukkit.entity.Player
import org.litote.kmongo.combine
import org.litote.kmongo.setValue
import java.util.UUID

@CommandPermission("ion.npc.command")
@CommandAlias("ionnpc")
object IonNPCCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("ionNPCTypes") { _ -> DatabaseNPCTypes.allKeys() }
		manager.commandContexts.registerContext(DatabaseNPCType::class.java) { context ->
			val name = context.popFirstArg()
			DatabaseNPCTypes.getByIdentifier(name)
		}
//		manager.commandCompletions.register
	}

	@Subcommand("create")
	@CommandCompletion("@ionNPCTypes @nothing")
	fun create(sender: Player, type: DatabaseNPCType<*>) {
		type.createNew(sender, Skins[sender.uniqueId]!!)
	}

	/** Map of player UUID to NPC UUID */
	private val selectionMap: MutableMap<UUID, UUID> = mutableMapOf()

	private fun requireSelectedNPC(sender: Player): UniversalNPCWrapper<*, *> = selectionMap[sender.uniqueId]?.let(DatabaseNPCs::getWrapped) ?: fail { "You don't have a NPC selected!" }
	private fun requireManagedNPC(sender: Player, npc: UniversalNPCWrapper<*, *>): Unit = failIf(!npc.canManage(sender)) { "You can't manage that NPC!" }

	@Subcommand("select|sel")
	fun onSelect(sender: Player) {
		val nearest = DatabaseNPCs.getAll().filter {
			it.npc.storedLocation.world.uid == sender.world.uid &&
			it.npc.storedLocation.distance(sender.location) < 5.0
		}.minByOrNull { it.npc.storedLocation.distance(sender.location) }

		if (nearest == null) fail { "You aren't near any NPC!" }
		requireManagedNPC(sender, nearest)

		sender.success("Selected ${nearest.npc.name}.")
		selectionMap[sender.uniqueId] = nearest.npc.uniqueId
	}

	@Subcommand("deselect")
	fun onDeselect(sender: Player) {
		selectionMap.remove(sender.uniqueId) ?: fail { "You don't have a NPC selected!" }
		sender.success("De-selected npc.")
	}

	@Subcommand("remove|delete")
	fun remove(sender: Player) = asyncCommand(sender) {
		val selected = requireSelectedNPC(sender)
		requireManagedNPC(sender, selected)

		if (!DatabaseNPCs.remove(selected.npc.uniqueId)) sender.serverError("Could not remove NPC.")
		else sender.success("Removed ${selected.npc.name}.")
	}

	@Subcommand("set skin")
	fun setSkin(sender: Player, skin: String) = asyncCommand(sender) {
		val selected = requireSelectedNPC(sender)
		requireManagedNPC(sender, selected)

		val id: UUID = getIdFromName(skin)
		val skinData: Skins.SkinData = Skins[id] ?: fail { "Failed to retrieve skin for $skin!" }

		UniversalNPC.updateSkinData(selected.oid, skinData.toBytes())
		sender.success("Updated skin.")
	}

	@Subcommand("set location|position")
	fun setLocation(sender: Player) = asyncCommand(sender) {
		val selected = requireSelectedNPC(sender)
		requireManagedNPC(sender, selected)

		if (sender.world.uid != selected.npc.storedLocation.world.uid) fail { "You can't move a NPC to another world!" }

		UniversalNPC.updateById(selected.oid, combine(
			setValue(UniversalNPC::x, sender.location.x),
			setValue(UniversalNPC::y, sender.location.y),
			setValue(UniversalNPC::z, sender.location.z),
		))

		sender.success("Moved NPC.")
	}

	@Subcommand("manage")
	fun manageMetaData(sender: Player) = asyncCommand(sender) {
		val selected = requireSelectedNPC(sender)
		requireManagedNPC(sender, selected)

		selected.manage(sender)
	}
}
