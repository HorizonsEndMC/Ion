package net.horizonsend.ion.server.features.multiblock.type.power.storage

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers.newMultiblockSignOverlay
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.kyori.adventure.text.Component.empty
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

abstract class PowerBankMultiblock(tierText: String) : Multiblock(), NewPoweredMultiblock<PowerBankMultiblock.PowerBankEntity>, InteractableMultiblock {
	abstract val tierMaterial: Material
	override val name = "powerbank"

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

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val b = getMultiblockEntity(sign) ?: return
		player.information("b: ${b.storage.getPower()}")
	}

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	): PowerBankEntity {
		return PowerBankEntity(
			data,
			manager,
			this,
			x,
			y,
			z,
			world,
			structureDirection
		)
	}

	class PowerBankEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: PowerBankMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity, LegacyMultiblockEntity {
		override val storage: PowerStorage = loadStoredPower(data)

		private val displayHandler = newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

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

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
		}

		override val powerInputOffset: Vec3i = Vec3i(0, -1, 0)

		override fun loadFromSign(sign: Sign) {
			val oldPower = sign.persistentDataContainer.get(NamespacedKeys.POWER, PersistentDataType.INTEGER) ?: return

			storage.setPower(oldPower)

			sign.persistentDataContainer.remove(NamespacedKeys.POWER)
			sign.front().line(2, empty())
			sign.update()
		}
	}
}
