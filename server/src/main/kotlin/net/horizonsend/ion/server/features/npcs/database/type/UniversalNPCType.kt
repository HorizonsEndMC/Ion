package net.horizonsend.ion.server.features.npcs.database.type

import net.citizensnpcs.api.npc.NPC
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCs
import net.horizonsend.ion.server.features.npcs.database.metadata.UniversalNPCMetadata
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.function.Consumer
import kotlin.reflect.KClass

interface UniversalNPCType<T: UniversalNPCMetadata> {
	val metaTypeClass: KClass<T>
	val identifier: String

	/**
	 * Creates a new NPC of this type at the provided location, with the provided skin.
	 **/
	fun createNew(location: Location, skinData: Skins.SkinData) {
		UniversalNPCs.create(location, this, getDefaultMetaData(), skinData)
	}

	/**
	 * Creates a new NPC of this type at the provided location, with the provided skin.
	 * The player will be checked for the ability to use this type, and recieve feedback if it was successful.
	 **/
	fun createNew(player: Player, skinData: Skins.SkinData) {
		UniversalNPCs.create(player, player.location, this, getDefaultMetaData(player), skinData)
	}

	open fun onSpawn(wrapper: UniversalNPCWrapper<*, *>) {}
	open fun onRemove(wrapper: UniversalNPCWrapper<*, *>) {}

	/** Returns the metadata from string storage */
	fun deSerializeMetaData(raw: String): T {
		return Configuration.parse(metaTypeClass, raw)
	}

	/** Returns the serialized metadata for storage */
	fun serializeMetaData(raw: T): String {
		return Configuration.write(metaTypeClass, raw)
	}

	/**
	 * Returns the stock metadata of this NPC
	 **/
	fun getDefaultMetaData(): T

	/**
	 * Returns the stock metadata of this NPC, with the player available.
	 **/
	fun getDefaultMetaData(creator: Player): T = getDefaultMetaData()

	/**
	 * Code to update the NPC if the metadata changes.
	 **/
	fun handleMetaDataChange(new: String, npc: UniversalNPCWrapper<*, *>)

	/**
	 * Applies any additional traits to the NPC during spawning / loading
	 **/
	fun applyTraits(npc: NPC, metaData: T) {}

	/**
	 * Returns the display name of the NPC
	 **/
	fun getDisplayName(metaData: UniversalNPCMetadata): Component

	/**
	 * Checks if this NPC type is allowable at this location
	 *
	 * @param player: The player creating or moving this NPC
	 **/
	fun checkLocation(player: Player, location: Location): Boolean

	/**
	 * Returns whether the player may use this type of NPC (spawn it).
	 *
	 * @param player: The player to check
	 **/
	fun canCreate(player: Player, metaData: T): Boolean

	/**
	 * Returns whether the player can manage this individual NPC.
	 *
	 * @param player: The player to check
	 **/
	fun canManage(player: Player, wrapper: UniversalNPCWrapper<*, T>): Boolean

	/**
	 * Any code to be run when a player right-clicks on this NPC.
	 **/
	fun handleClick(player: Player, npc: UniversalNPCWrapper<*, T>, metaData: T)

	/**
	 * Any code handle the management of this NPC. The edited metadata is accepted through the consumer, then applied.
	 **/
	fun manage(player: Player, managed: UniversalNPCWrapper<*, T>, newMetaDataConsumer: Consumer<T>)
}
