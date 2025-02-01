package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import kotlin.math.abs

object MobDefender : Multiblock(), EntityMultiblock<MobDefender.MobDefenderEntity>, DisplayNameMultilblock {
	override val name = "mobdefender"

	override val signText = createSignText(
		line1 = "&cHostile",
		line2 = "&cCreature",
		line3 = "&7Control",
		line4 = "MobDefender Co"
	)

	override val displayName: Component get() = text("Mob Defender")
	override val description: Component get() = text("Prevents hostile mobs from spawning in a 99-block wide cube centered around this multiblock.")

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).solidBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}

		z(+0) {
			y(-1) {
				x(-1).solidBlock()
				x(+0).diamondBlock()
				x(+1).solidBlock()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).solidBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		player.information("Created mob defender.")
	}

	fun cancelSpawn(location: Location): Boolean {
		if (location.world.name.lowercase().contains("eden")) return false
		val mobDefenders = location.world.ion.multiblockManager[MobDefenderEntity::class]

		return mobDefenders.asSequence()
			.filter { it.world == location.world }
			.filter {
				val globalVec3i = it.globalVec3i
				abs(location.x - globalVec3i.x) < 50  &&
				abs(location.y - globalVec3i.y) < 50 &&
				abs(location.z - globalVec3i.z) < 50
			}
			.any { it.isIntact() }
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): MobDefenderEntity {
		return MobDefenderEntity(manager, x, y, z, world, structureDirection)
	}

	class MobDefenderEntity(
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace
	) : MultiblockEntity(manager, MobDefender, world, x, y, z, signDirection) {
		override val inputsData: InputsData = none()

		override fun onLoad() {
			world.ion.multiblockManager.register(this)
		}

		override fun handleRemoval() {
			world.ion.multiblockManager.register(this)
		}

		override fun onUnload() {
			world.ion.multiblockManager.deregister(this)
		}
	}
}
