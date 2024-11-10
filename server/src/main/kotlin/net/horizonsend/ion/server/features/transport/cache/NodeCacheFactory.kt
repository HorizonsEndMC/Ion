package net.horizonsend.ion.server.features.transport.cache

import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import kotlin.reflect.KClass

class NodeCacheFactory private constructor(private val materialHandlers: Map<Material, MaterialHandler<*>>) {
	fun cache(block: Block): CachedNode? {
		val type = block.getTypeSafe() ?: return null
		val materialFactory = materialHandlers[type] ?: return null
		val blockData = getBlockDataSafe(block.world, block.x, block.y, block.z) ?: return null

		if (!materialFactory.blockDataClass.isInstance(blockData)) return null
		return materialFactory.construct(blockData)
	}

	class Builder	{
		val materialHandlers = mutableMapOf<Material, MaterialHandler<*>>()

		inline fun <reified T: BlockData> addDataHandler(material: Material, noinline constructor: (T) -> CachedNode): Builder {
			this.materialHandlers[material] = MaterialHandler<T>(T::class, constructor)
			return this
		}

		fun addSimpleNode(material: Material, node: CachedNode): Builder {
			this.materialHandlers[material] = MaterialHandler(BlockData::class) { node }
			return this
		}

		fun build(): NodeCacheFactory {
			return NodeCacheFactory(materialHandlers)
		}
	}

	class MaterialHandler<T: BlockData>(val blockDataClass: KClass<T>, val constructor: (T) -> CachedNode) {
		fun construct(blockData: BlockData): CachedNode {
			@Suppress("UNCHECKED_CAST")
			return constructor.invoke(blockData as T)
		}
	}

	companion object {
		fun builder(): Builder = Builder()
	}
}
