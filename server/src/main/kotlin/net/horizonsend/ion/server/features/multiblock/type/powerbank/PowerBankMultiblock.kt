package net.horizonsend.ion.server.features.multiblock.type.powerbank

import net.horizonsend.ion.server.features.client.display.container.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimpleTextDisplayPoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimpleTextDisplayPoweredMultiblockEntity.Companion.createTextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

abstract class PowerBankMultiblock(tierText: String) : Multiblock(), EntityMultiblock<PowerBankMultiblock.PowerBankEntity> {
	abstract val tierMaterial: Material
	override val name = "powerbank"

	abstract val maxPower: Int

	override val signText = createSignText(
		line1 = "&2Power &8Bank",
		line2 = "&4------",
		line3 = null,
		line4 = tierText
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).extractor()
				x(+0).wireInputComputer()
				x(+1).extractor()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			for (i in -1..1) {
				y(i) {
					x(-1).anyGlass()
					x(+0).redstoneBlock()
					x(+1).anyGlass()
				}
			}
		}

		z(+2) {
			y(-1) {
				x(-1).type(tierMaterial)
				x(+0).anyGlass()
				x(+1).type(tierMaterial)
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun createEntity(
		manager: ChunkMultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	): PowerBankEntity {
		return PowerBankEntity(
			manager,
			this,
			x,
			y,
			z,
			world,
			structureDirection,
			maxPower,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}

	class PowerBankEntity(
		manager: ChunkMultiblockManager,
		multiblock: Multiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
		override val maxPower: Int,
		override var powerUnsafe: Int = 0
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), SimpleTextDisplayPoweredMultiblockEntity {
		override val displayHandler: TextDisplayHandler = createTextDisplayHandler(this)

		override fun onLoad() {
			register()
			displayHandler.update()
		}

		override fun onUnload() {
			unRegister()
			displayHandler.remove()
		}

		override fun handleRemoval() {
			unRegister()
			displayHandler.remove()
		}

		override fun isValid(): Boolean {
			return !removed
		}

		override fun storeAdditionalData(store: PersistentMultiblockData) {
			store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, getPower())
		}

		override fun toString(): String {
			return "POWER BANK TIER: $multiblock! Power: ${getPower()}, Facing: $structureDirection"
		}
	}
}
