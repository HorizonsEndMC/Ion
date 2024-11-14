package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import kotlin.reflect.KClass

class NodeCacheFactory private constructor(private val materialHandlers: Map<Material, MaterialHandler<*>>) {
	fun cache(block: Block): Node? {
		val type = block.getTypeSafe() ?: return null
		val materialFactory = materialHandlers[type] ?: return null
		val blockData = getBlockDataSafe(block.world, block.x, block.y, block.z) ?: return null

		if (!materialFactory.blockDataClass.isInstance(blockData)) return null
		return materialFactory.construct(blockData, toBlockKey(block.x, block.y, block.z))
	}

	class Builder	{
		val materialHandlers = mutableMapOf<Material, MaterialHandler<*>>()

		inline fun <reified T: BlockData> addDataHandler(material: Material, noinline constructor: (T, BlockKey) -> Node): Builder {
			this.materialHandlers[material] = MaterialHandler(T::class, constructor)
			return this
		}

		fun addSimpleNode(materials: Iterable<Material>, constructor: (BlockKey) -> Node): Builder {
			for (material in materials) this.materialHandlers[material] = MaterialHandler(BlockData::class) { _, key -> constructor(key) }
			return this
		}

		fun addSimpleNode(material: Material, constructor: (BlockKey) -> Node): Builder {
			this.materialHandlers[material] = MaterialHandler(BlockData::class) { _, key -> constructor(key) }
			return this
		}

		fun addSimpleNode(material: Material, node: Node): Builder {
			this.materialHandlers[material] = MaterialHandler(BlockData::class) { _, _ -> node }
			return this
		}

		fun addSimpleNode(materials: Iterable<Material>, node: Node): Builder {
			for (material in materials) this.materialHandlers[material] = MaterialHandler(BlockData::class) { _, _ -> node }
			return this
		}

		fun build(): NodeCacheFactory {
			return NodeCacheFactory(materialHandlers)
		}
	}

	class MaterialHandler<T: BlockData>(val blockDataClass: KClass<T>, val constructor: (T, BlockKey) -> Node) {
		fun construct(blockData: BlockData, key: BlockKey): Node {
			@Suppress("UNCHECKED_CAST")
			return constructor.invoke(blockData as T, key)
		}
	}

	companion object {
		fun builder(): Builder = Builder()
	}
}
