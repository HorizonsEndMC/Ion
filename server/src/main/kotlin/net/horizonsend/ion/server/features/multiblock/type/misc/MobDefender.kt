package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import kotlin.math.abs

object MobDefender : Multiblock(), EntityMultiblock<MobDefender.MobDefenderEntity> {
	override val name = "mobdefender"

	override val signText = createSignText(
		line1 = "&cHostile",
		line2 = "&cCreature",
		line3 = "&7Control",
		line4 = "MobDefender Co"
	)

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
			.filter { abs(location.x - it.x) < 50 }
			.filter { abs(location.y - it.y) < 50 }
			.filter { abs(location.z - it.z) < 50 }
			.mapNotNull { getBlockIfLoaded(it.world, it.x, it.y, it.z) }
			.mapNotNull { it.getState(false) as? Sign }
			.any { signMatchesStructure(it, loadChunks = false) }
	}

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, signOffset: BlockFace): MobDefenderEntity {
		return MobDefenderEntity(manager, x, y, z, world, signOffset)
	}

	class MobDefenderEntity(
		manager: ChunkMultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace
	) : MultiblockEntity(manager, MobDefender, x, y, z, world, signDirection)
}
