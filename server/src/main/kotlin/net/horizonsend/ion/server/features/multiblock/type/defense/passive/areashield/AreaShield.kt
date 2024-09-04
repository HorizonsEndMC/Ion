package net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.UpdatedPowerDisplayEntity
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.POWER
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getSphereBlocks
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType.INTEGER
import java.util.concurrent.TimeUnit

abstract class AreaShield(val radius: Int) : Multiblock(), NewPoweredMultiblock<AreaShield.AreaShieldEntity>, InteractableMultiblock {
	override fun onTransformSign(player: Player, sign: Sign) {
		player.success("Area Shield created.")
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val blocks: List<Vec3i> = getSphereBlocks(radius)

		val world = sign.world
		val (x0, y0, z0) = Vec3i(sign.location)

		val start = System.nanoTime()
		Tasks.bukkitRunnable {
			for ((dx, dy, dz) in blocks) {
				val x = x0 + dx + 0.5
				val y = y0 + dy + 0.5
				val z = z0 + dz + 0.5
				world.spawnParticle(
					Particle.BLOCK_MARKER,
					x,
					y,
					z,
					1,
					0.0,
					0.0,
					0.0,
					0.0,
					Material.BARRIER.createBlockData()
				)
			}

			if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(10L)) {
				cancel()
			}
		}.runTaskTimer(IonServer, 20, 20)
	}

	override val name get() = "areashield"

	override val maxPower = 100_000

	override val signText = createSignText(
		"&6Area",
		"&bParticle Shield",
		null,
		"&8Radius: &a$radius"
	)

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AreaShieldEntity {
		return AreaShieldEntity(
			manager,
			this,
			x,
			y,
			z,
			world,
			structureDirection,
			data.getAdditionalDataOrDefault(POWER, INTEGER, 0),
			maxPower
		)
	}

	class AreaShieldEntity(
		manager: ChunkMultiblockManager,
		override val multiblock: AreaShield,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
		override var powerUnsafe: Int,
		override val maxPower: Int
	) : MultiblockEntity(manager, multiblock, x, y, z, world, signDirection), UpdatedPowerDisplayEntity {
		override val displayUpdates: MutableList<(UpdatedPowerDisplayEntity) -> Unit> = mutableListOf()

		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, structureDirection.oppositeFace, 0.5f)
		).register()

		override fun onLoad() {
			world.ion.multiblockManager.register(this)

			displayHandler.update()
		}

		override fun handleRemoval() {
			world.ion.multiblockManager.deregister(this)

			displayHandler.remove()
		}

		override fun onUnload() {
			world.ion.multiblockManager.deregister(this)

			displayHandler.remove()
		}

		override fun storeAdditionalData(store: PersistentMultiblockData) {
			store.addAdditionalData(POWER, INTEGER, getPower())
		}

		override fun toString(): String = "AREA SHIELD::: POWER:: ${getPower()}"

	}
}
