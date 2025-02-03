package net.horizonsend.ion.server.features.custom.blocks.extractor

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.InteractableCustomBlock
import net.horizonsend.ion.server.features.custom.blocks.WrenchRemovable
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getCustomName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class CustomExtractorBlock<T: ExtractorData>(
    identifier: String,
    blockData: BlockData,
    drops: BlockLoot,
    customBlockItem: Supplier<CustomBlockItem>,
	val extractorDataType: KClass<T>
) : InteractableCustomBlock(identifier, blockData, drops, customBlockItem), WrenchRemovable  {
	val cooldown = PerPlayerCooldown(5L)

	fun load(key: BlockKey, container: MetaDataContainer<*, *>): T {
		return createExtractorData(key, PDCSerializers.unpack(container))
	}

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		if (event.player.isSneaking) return
		val chunk = IonChunk[block.world, block.x.shr(4), block.z.shr(4)] ?: return

		val key = toBlockKey(block.x, block.y, block.z)

		val extractorManager = chunk.transportNetwork.extractorManager
		val extractorData = extractorManager.getExtractorData(key) ?: extractorManager.registerExtractor(key)

		if (extractorData == null) return

		if (!extractorDataType.isInstance(extractorData)) return

		event.isCancelled = true

		cooldown.tryExec(event.player) {
			@Suppress("UNCHECKED_CAST")
			openGUI(event.player, block, extractorData as T)
		}
	}

	abstract fun createExtractorData(pos: BlockKey): T

	abstract fun createExtractorData(pos: BlockKey, metaData: ExtractorMetaData): T

	abstract fun openGUI(player: Player, block: Block, extractorData: T)

	override fun decorateItem(itemStack: ItemStack, block: Block) {
		val state = block.state
		state as TileState

		val data = state.persistentDataContainer.get(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer) ?: return

		val customDisplayName = itemStack.getCustomName()

		itemStack
			.updateDisplayName(ofChildren(customDisplayName.orEmpty(), Component.text(" (Configured)")))
			.updatePersistentDataContainer {
				set(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer, data)
			}
	}
}
