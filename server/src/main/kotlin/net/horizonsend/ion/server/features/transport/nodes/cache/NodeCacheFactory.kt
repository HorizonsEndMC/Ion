package net.horizonsend.ion.server.features.transport.nodes.cache

import com.google.common.collect.Multimap
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import kotlin.reflect.KClass

class NodeCacheFactory private constructor(private val materialHandlers: Multimap<Material, MaterialHandler<*>>) {
	fun cache(block: Block, holder: CacheHolder<*>): Node? {
		val type = block.getTypeSafe() ?: return null

		val forMaterial = materialHandlers[type] ?: return null
		val blockData = getBlockDataSafe(block.world, block.x, block.y, block.z) ?: return null

		val filtered = forMaterial.filter { handler -> handler.blockDataClass.isInstance(blockData) }

		return filtered.firstNotNullOfOrNull { handler -> handler.construct(blockData, toBlockKey(block.x, block.y, block.z), holder) }
	}

	class Builder	{
		val materialHandlers = multimapOf<Material, MaterialHandler<*>>()

		inline fun <reified T: BlockData> addDataHandler(material: Material, noinline constructor: (T, BlockKey, CacheHolder<*>) -> Node?): Builder {
			this.materialHandlers[material].add(MaterialHandler(T::class, constructor))
			return this
		}

		inline fun <reified T: BlockData> addDataHandler(customBlock: CustomBlock, noinline constructor: (T, BlockKey, CacheHolder<*>) -> Node?): Builder {
			require(customBlock.blockData is T)

			return addDataHandler<T>(customBlock.blockData.material) { data, lng, holder ->
				if (CustomBlocks.getByBlockData(data) != customBlock) return@addDataHandler null
				constructor.invoke(data, lng, holder)
			}
		}

		inline fun <reified T: BlockData> addDataHandler(materials: Iterable<Material>, noinline constructor: (T, BlockKey, CacheHolder<*>) -> Node): Builder {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(T::class, constructor))
			return this
		}

		inline fun <reified T: BlockData> addDataHandler(vararg materials: Material, noinline constructor: (T, BlockKey, CacheHolder<*>) -> Node?): Builder {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(T::class, constructor))
			return this
		}

		fun addSimpleNode(materials: Iterable<Material>, constructor: (BlockKey, Material, CacheHolder<*>) -> Node): Builder {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(BlockData::class) { data, key, holder -> constructor(key, data.material, holder) })
			return this
		}

		fun addSimpleNode(vararg materials: Material, constructor: (BlockKey, Material, CacheHolder<*>) -> Node): Builder {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(BlockData::class) { data, key, holder -> constructor(key, data.material, holder) })
			return this
		}

		fun addSimpleNode(material: Material, constructor: (BlockKey, CacheHolder<*>) -> Node): Builder {
			this.materialHandlers[material].add(MaterialHandler(BlockData::class) { _, key, holder -> constructor(key, holder) })
			return this
		}

		fun addSimpleNode(material: Material, node: Node): Builder {
			this.materialHandlers[material].add(MaterialHandler(BlockData::class) { _, _, _ -> node })
			return this
		}

		inline fun <reified T: BlockData> addSimpleNode(customBlock: CustomBlock, node: Node): Builder {
			return addDataHandler<T>(customBlock.blockData.material) { data, lng, _ ->
				if (CustomBlocks.getByBlockData(data) != customBlock) return@addDataHandler null
				node
			}
		}

		fun addSimpleNode(materials: Iterable<Material>, node: Node): Builder {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(BlockData::class) { _, _, _ -> node })
			return this
		}

		fun addSimpleNode(vararg material: Material, node: Node) = addSimpleNode(material.toSet(), node)

		fun build(): NodeCacheFactory {
			return NodeCacheFactory(materialHandlers)
		}
	}

	class MaterialHandler<T: BlockData>(val blockDataClass: KClass<T>, val constructor: (T, BlockKey, CacheHolder<*>) -> Node?) {
		@Synchronized
		fun construct(blockData: BlockData, key: BlockKey, holder: CacheHolder<*>): Node? {
			@Suppress("UNCHECKED_CAST")
			return runCatching { constructor.invoke(blockData as T, key, holder) }
				.onFailure { exception -> exception.printStackTrace() }
				.getOrNull()
		}
	}

	companion object {
		fun builder(): Builder = Builder()
	}
}
