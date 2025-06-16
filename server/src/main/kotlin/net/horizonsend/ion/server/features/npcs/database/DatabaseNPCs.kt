package net.horizonsend.ion.server.features.npcs.database

import com.google.common.collect.Multimap
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.trait.SkinTrait
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.binary
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.UniversalNPC
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.npcs.NPCManager
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.npcs.database.type.DatabaseNPCType
import net.horizonsend.ion.server.features.npcs.database.type.DatabaseNPCTypes
import net.horizonsend.ion.server.features.npcs.isCitizensLoaded
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import java.util.UUID

object DatabaseNPCs : IonServerComponent(true) {
	private val npcManager = NPCManager(log, "DatabaseNPCs")
	private val wrapperMap: MutableMap<UUID, UniversalNPCWrapper<*, *>> = mutableMapOf()
	private val typeMap: Multimap<DatabaseNPCType<*>, UUID> = multimapOf()
	private val oidMap: MutableMap<Oid<UniversalNPC>, UUID> = mutableMapOf()

	override fun onEnable() {
		if (!isCitizensLoaded) return
		npcManager.enableRegistry()

		loadNPCs()
		watchChanges()
	}

	override fun onDisable() {
		npcManager.disableRegistry()
	}

	private fun loadNPCs() {
		Tasks.async {
			UniversalNPC.all()
		}
	}

	private fun watchChanges() {
		UniversalNPC.watchInserts { it.fullDocument?.let(::addNew) }
		UniversalNPC.watchDeletes(fullDocument = true) { it.fullDocument?.let(::handleRemoval) }
		UniversalNPC.watchUpdates { change ->
			val npc = oidMap[change.oid] ?: return@watchUpdates
			val wrapped = wrapperMap[npc] ?: return@watchUpdates

			change[UniversalNPC::jsonMetadata]?.let { wrapped.type.handleMetaDataChange(it.string(), wrapped) }

			change[UniversalNPC::skinData]?.let { handleSkinChange(change.oid, it.binary()) }

			var locationChanged = false
			change[UniversalNPC::worldKey]?.let { locationChanged = true }
			change[UniversalNPC::x]?.let { locationChanged = true }
			change[UniversalNPC::y]?.let { locationChanged = true }
			change[UniversalNPC::z]?.let { locationChanged = true }

			if (locationChanged) {
				handleMovement(change.oid)
			}
		}
	}

	fun <M : UniversalNPCMetadata, T: DatabaseNPCType<M>> spawn(player: Player, location: Location, type: T, metadata: M, skinData: Skins.SkinData) {
		if (!isCitizensLoaded) {
			player.userError("Citizens is not loaded! NPCs will not function.")
			return
		}

		if (!type.canUseType(player, metadata)) {
			player.userError("You cannot use this NPC type!")
			return
		}

		UniversalNPC.create(
			location.world.key.value(),
			location.x,
			location.y,
			location.z,
			skinData.toBytes(),
			type.identifier,
			type.serializeMetaData(metadata)
		)
	}

	private fun addNew(document: UniversalNPC) = Tasks.sync {
		val type = DatabaseNPCTypes.getByIdentifier(document.typeId)
		val metaData = type.deSerializeMetaData(document.jsonMetadata)
		val location = document.bukkitLocation() ?: return@sync

		npcManager.createNPC(
			legacyAmpersand.serialize(type.getDisplayName(metaData)),
			document.npcID,
			4000 + npcManager.allNPCs().size,
			location,
			preCheck = {
				if (!oidMap.containsKey(document._id) && !wrapperMap.containsKey(document.npcID)) {
					return@createNPC true
				}

				log.warn("NPC tried to spawn twice!")

				false
			},
			callback = { npc ->
				val skin = Skins.SkinData.fromBytes(document.skinData)
				npc.getOrAddTrait(SkinTrait::class.java).apply {
					setSkinPersistent(npc.name, skin.signature, skin.value)
				}

				val wrapped = UniversalNPCWrapper(npc, type, metaData)
				wrapped.applyTraits()

				wrapperMap[document.npcID] = wrapped
				typeMap[type].add(document.npcID)
				oidMap[document._id] = document.npcID
			}
		)
	}

	fun handleRemoval(document: UniversalNPC) {
		npcManager.removeNPC(document.npcID)

		wrapperMap.remove(document.npcID)
		typeMap[DatabaseNPCTypes.getByIdentifier(document.typeId)].remove(document.npcID)
		oidMap.remove(document._id)
	}

	fun handleMovement(id: Oid<UniversalNPC>) {

	}

	fun handleSkinChange(id: Oid<UniversalNPC>, new: ByteArray) {

	}

	fun UniversalNPC.bukkitWorld(): World? {
		return Bukkit.getWorld(NamespacedKey.fromString(worldKey) ?: return null)
	}

	fun UniversalNPC.bukkitLocation(): Location? {
		return Location(bukkitWorld() ?: return null, x, y, z)
	}

	@EventHandler
	fun onNPCCLick(event: NPCRightClickEvent) {
		val wrapper = wrapperMap[event.npc.uniqueId] ?: return
		wrapper.handleClick(event.clicker)
	}
}
