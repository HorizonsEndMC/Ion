package net.horizonsend.ion.server.features.multiblock.entity.type

import com.google.common.collect.Multimap
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.isCopperBulb
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.block.Block
import org.bukkit.block.data.type.CopperBulb
import kotlin.math.roundToInt

interface GaugedMultiblockEntity {
	val gauges: MultiblockGauges

	fun tickGauges() {
		gauges.tickGauges()
	}

	class MultiblockGauges private constructor(val entity: MultiblockEntity, val gauges: Multimap<Vec3i, GaugeData>) {
		fun tickGauges() {
			for (offset in gauges.keys()) {
				val gauges = gauges.get(offset)
				val block = entity.getBlockRelative(offset.x, offset.y, offset.z)
				val gauge = gauges.firstOrNull { data -> data.blockMatch.invoke(block) } ?: return
				handleGauge(gauge, block)
			}
		}

		fun handleGauge(gauge: GaugeData, block: Block) {
			gauge.claimOwnership.invoke(entity, block)
			val signal = gauge.readValue.invoke()
			gauge.applyValue.invoke(signal, block)
		}

		class Builder(val entity: MultiblockEntity) {
			private val multimap = multimapOf<Vec3i, GaugeData>()

			fun addGauge(offset: Vec3i, data: GaugeData): Builder {
				multimap[offset].add(data)
				return this
			}

			fun addGauge(offsetX: Int, offsetY: Int, offsetZ: Int, data: GaugeData): Builder {
				return addGauge(Vec3i(offsetX, offsetY, offsetZ), data)
			}

			fun build(): MultiblockGauges {
				return MultiblockGauges(entity, multimap)
			}
		}

		companion object {
			fun builder(entity: MultiblockEntity) = Builder(entity)
		}
	}

	class GaugeData(val blockMatch: (Block) -> Boolean, val claimOwnership: (MultiblockEntity, Block) -> Unit, val readValue: () -> Int, val applyValue: (Int, Block) -> Boolean) {
		companion object {
			fun fluidTemperatureGauge(store: FluidStorageContainer, multiblock: MultiblockEntity) = GaugeData(
				blockMatch = { it.customBlock?.key == CustomBlockKeys.TEMPERATURE_GAUGE },
				claimOwnership = { entity, block -> CustomBlockKeys.TEMPERATURE_GAUGE.getValue().setMultiblockOwner(entity.manager, Vec3i(block.x, block.y, block.z), entity) },
				readValue = { store.getContents().getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, multiblock.location).value.roundToInt().coerceIn(0, 15) },
				applyValue = { signal, block -> CustomBlockKeys.TEMPERATURE_GAUGE.getValue().setSignalOutput(signal, block.world, Vec3i(block.x, block.y, block.z)) }
			)
			fun onOffGauge(activeSignal: () -> Boolean) = GaugeData(
				blockMatch = { it.type.isCopperBulb },
				claimOwnership = { _, _ ->  },
				readValue = { if (activeSignal.invoke()) 15 else 0 },
				applyValue = { signal, block ->
					val existing = block.blockData as CopperBulb
					if (existing.isLit == (signal == 15)) return@GaugeData true

					existing.isLit = signal == 15

					Tasks.sync {
						block.blockData = existing
					}
					true
				}
			)
		}
	}
}
