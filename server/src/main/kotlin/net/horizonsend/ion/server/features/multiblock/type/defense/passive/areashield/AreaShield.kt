package net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getSphereBlocks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.TimeUnit

abstract class AreaShield(val radius: Int) : Multiblock(), EntityMultiblock<AreaShield.AreaShieldEntity>, InteractableMultiblock, DisplayNameMultilblock {
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

	val shieldText = "&8Radius: &a$radius"
	override val name = "areashield"
	override val signText = createSignText(
		"&6Area",
		"&bParticle Shield",
		null,
		shieldText,
	)

	override val displayName: Component get() = ofChildren(text("Area Shield "), text("("), legacyAmpersand.deserialize(shieldText), text(")"))
	override val description: Component get() = text("Prevents explosions within a $radius radius.")

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AreaShieldEntity {
		return AreaShieldEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class AreaShieldEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: AreaShield,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace,
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureFace, 100_000), LegacyMultiblockEntity {
		override val displayHandler: TextDisplayHandler = standardPowerDisplay(this)

		override fun onLoad() {
			world.ion.multiblockManager.register(this)
			super.onLoad()
		}

		override fun handleRemoval() {
			world.ion.multiblockManager.deregister(this)
			super.handleRemoval()
		}

		override fun onUnload() {
			world.ion.multiblockManager.deregister(this)
			super.onUnload()
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
