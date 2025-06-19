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
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.npcs.NPCManager
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.features.npcs.database.type.UniversalNPCType
import net.horizonsend.ion.server.features.npcs.database.type.UniversalNPCTypes
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
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

object UniversalNPCs : IonServerComponent(true) {
	private val npcManager = NPCManager(log, "UniversalNPCs")
	private val wrapperMap: MutableMap<UUID, UniversalNPCWrapper<*, *>> = mutableMapOf()
	private val typeMap: Multimap<UniversalNPCType<*>, UUID> = multimapOf()
	private val oidMap: MutableMap<Oid<UniversalNPC>, UUID> = mutableMapOf()

	fun getAll() = wrapperMap.values
	fun <M : UniversalNPCMetadata, T: UniversalNPCType<M>> getAll(type: T): List<UniversalNPCWrapper<T, M>> = typeMap[type].map(wrapperMap::get).filterIsInstance<UniversalNPCWrapper<T, M>>()
	fun getWrapped(npcId: UUID) = wrapperMap[npcId]

	override fun onEnable() {
		if (!isCitizensLoaded) return
		npcManager.enableRegistry()

		// Load NPCs from storage
		Tasks.async { UniversalNPC.all().forEach(::loadNPCFromStorage) }

		// Watch changes in stored NPCs
		UniversalNPC.watchInserts { it.fullDocument?.let(::loadNPCFromStorage) }
		UniversalNPC.watchDeletes { it.oid.let(::handleRemoval) }
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

	override fun onDisable() {
		npcManager.disableRegistry()
	}

	fun <M : UniversalNPCMetadata, T: UniversalNPCType<M>> create(player: Player, location: Location, type: T, metadata: M, skinData: Skins.SkinData): Boolean {
		if (!isCitizensLoaded) {
			player.userError("Citizens is not loaded! NPCs will not function.")
			return false
		}

		if (!type.checkLocation(player, location)) {
			player.userError("You cannot use this NPC type in this location!")
			return false
		}

		if (!type.canUseType(player, metadata)) {
			player.userError("You cannot use this NPC type!")
			return false
		}

		val result = create(location, type, metadata, skinData)

		if (!result) {
			player.serverError("Could not create NPC!")
			return false
		}

		return true
	}

	fun <M : UniversalNPCMetadata, T: UniversalNPCType<M>> create(location: Location, type: T, metadata: M, skinData: Skins.SkinData): Boolean {
		if (!isCitizensLoaded) {
			return false
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

		return true
	}

	/**
	 * Removes this NPC from the database and the server
	 **/
	fun remove(npcId: UUID): Boolean {
		val wrapped = wrapperMap[npcId] ?: return false

		val result = UniversalNPC.delete(wrapped.oid)
		return result.deletedCount >= 1L
	}

	/**
	 * Loads the NPC from the stored info on the database
	 **/
	private fun loadNPCFromStorage(document: UniversalNPC) = Tasks.sync {
		val type = UniversalNPCTypes.getByIdentifier(document.typeId)
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

				val wrapped = UniversalNPCWrapper(npc, document._id, type, metaData)
				wrapped.applyTraits()

				wrapperMap[document.npcID] = wrapped
				typeMap[type].add(document.npcID)
				oidMap[document._id] = document.npcID
			}
		)
	}

	/**
	 * Handles the removal of the NPC on the server when it is removed from the database.
	 **/
	fun handleRemoval(document: Oid<UniversalNPC>) {
		val npcId = oidMap[document] ?: return

		Tasks.sync {
			npcManager.removeNPC(npcId)

			wrapperMap.remove(npcId)
			typeMap.keys().toSet().forEach { typeMap[it].remove(npcId) }
			oidMap.remove(document)
		}
	}

	/**
	 * If the location of this NPC changes, moves it to its new location
	 **/
	private fun handleMovement(id: Oid<UniversalNPC>) {
		Tasks.async {
			val npcId = oidMap[id] ?: return@async
			val wrapped = wrapperMap[npcId] ?: return@async
			val fullDocument = UniversalNPC.findById(id) ?: return@async

			val newLocation = fullDocument.bukkitLocation() ?: return@async

			Tasks.sync {
				wrapped.npc.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
			}
		}
	}

	/**
	 * If the skin of this NPC changes, updates it on the server
	 **/
	private fun handleSkinChange(id: Oid<UniversalNPC>, new: ByteArray) {
		Tasks.async {
			val npcId = oidMap[id] ?: return@async
			val wrapped = wrapperMap[npcId] ?: return@async

			val newSkin = Skins.SkinData.fromBytes(new)

			Tasks.sync {
				wrapped.npc.getOrAddTrait(SkinTrait::class.java).apply {
					setSkinPersistent(npc.name, newSkin.signature, newSkin.value)
				}
			}
		}
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
