package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.PoweredMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import kotlin.math.roundToInt

abstract class AbstractDisposalMultiblock : Multiblock(), PoweredMultiblock<AbstractDisposalMultiblock.DisposalMultiblockEntity> {
	override val name = "incinerator"

	override var signText: Array<Component?> = arrayOf(
		Component.text("Incinerator").color(NamedTextColor.RED),
		null,
		null,
		null
	)

	override val maxPower: Int = 150_000

	private val powerConsumed = 0.5
	abstract val mirrored: Boolean

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				if (!mirrored) x(-1).anyPipedInventory() else x(-1).ironBlock()
				x(+0).wireInputComputer()
				if (!mirrored) x(+1).ironBlock() else x(+1).anyPipedInventory()
			}
			y(0) {
				x(-1).anyStairs()
				x(+0).machineFurnace()
				x(+1).anyStairs()
			}
			y(+1) {
				x(0).anySlab()
			}
			z(+1) {
				y(-1) {
					x(-1).terracotta()
					x(+0).sponge()
					x(+1).terracotta()
				}
				y(+0) {
					x(-1).anyGlassPane()
					x(+0).type(Material.MAGMA_BLOCK)
					x(+1).anyGlassPane()
				}
				y(+1) {
					x(-1).anySlab()
					x(0).anySlab()
					x(+1).anySlab()
				}
			}
			z(+2) {
				y(-1) {
					x(-1).anyStairs()
					x(+0).redstoneBlock()
					x(+1).anyStairs()
				}
				y(+0) {
					x(-1).goldBlock()
					x(+0).anyGlassPane()
					x(+1).goldBlock()
				}
				y(+1) {
					x(0).anySlab()
				}
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): DisposalMultiblockEntity {
		return DisposalMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class DisposalMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: AbstractDisposalMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity, SyncTickingMultiblockEntity, LegacyMultiblockEntity {
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 20)
		val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		override fun tick() {
			val inventory = getInventory(if (this.multiblock.mirrored) 1 else -1, -1, 0) ?: return
			val power = powerStorage.getPower()
			if (power == 0) return tickingManager.sleep(20)

			var amountToClear = 0

			if (inventory.isEmpty) return tickingManager.sleep(50)

			// Clear while checking for power
			for (i in 0 until inventory.size) {
				val size = (inventory.getItem(i) ?: continue).amount
				if ((size * this.multiblock.powerConsumed) + (amountToClear * 3) >= power) continue
				amountToClear += size
				inventory.clear(i)
			}

			powerStorage.removePower((this.multiblock.powerConsumed * amountToClear).roundToInt())
		}

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}

object DisposalMultiblock : AbstractDisposalMultiblock() {
	override val mirrored = false
}

object DisposalMultiblockMirrored : AbstractDisposalMultiblock() {
	override val mirrored = true
}
