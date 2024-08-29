package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.client.display.container.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimpleTextDisplayPoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimpleTextDisplayPoweredMultiblockEntity.Companion.createTextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.SignMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

abstract class NewPowerBankMultiblock(tierText: String) : Multiblock(), EntityMultiblock<NewPowerBankMultiblock.PowerBankEntity>, SignMultiblock, InteractableMultiblock {
	abstract val tierMaterial: Material
	override val name = "newpowerbank"

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
		val world = sign.world
		val origin = getOrigin(sign)

		val entity = getMultiblockEntity(world, origin.x, origin.y, origin.z) as PowerBankEntity

		player.information("Entity: $entity")
		player.information("Removed: ${entity.removed}")
		player.information("Power: ${entity.getPower()}")
		player.information("Unsafe Power: ${entity.powerUnsafe}")
	}

	class PowerBankEntity(
        manager: ChunkMultiblockManager,
        multiblock: NewPowerBankMultiblock,
        x: Int,
        y: Int,
        z: Int,
        world: World,
        signDirection: BlockFace,
        override val maxPower: Int,
        override var powerUnsafe: Int = 0
	) : MultiblockEntity(manager, multiblock, x, y, z, world, signDirection), SimpleTextDisplayPoweredMultiblockEntity {
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
			return "POWER BANK TIER: $multiblock! Power: ${getPower()}, Facing: $facing"
		}
	}
}
