package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATE_DARK_RED
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object TestMultiblock : Multiblock(), EntityMultiblock<TestMultiblock.TestMultiblockEntity>, InteractableMultiblock {
	override val name: String = "testmulti1"

	override val signText: Array<Component?> = arrayOf(
		Component.text("Removed for brevity", PIRATE_DARK_RED, TextDecoration.BOLD),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0,0 ).type(Material.BEDROCK)
	}

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, signOffset: BlockFace): TestMultiblockEntity {
		return TestMultiblockEntity(
			manager,
			world,
			x,
			y,
			z,
			signOffset,
			data.getAdditionalData(NamespacedKeys.key("test"), PersistentDataType.STRING) ?: ""
		)
	}

	class TestMultiblockEntity(
        manager: ChunkMultiblockManager,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        signOffset: BlockFace,
        var string: String
	) : MultiblockEntity(manager, TestMultiblock, x, y, z, world, signOffset), SyncTickingMultiblockEntity {
		override fun storeAdditionalData(store: PersistentMultiblockData) {
			store.addAdditionalData(NamespacedKeys.key("test"), PersistentDataType.STRING, string)
		}

		var ticks = 0;

		override fun tick() {
			ticks++

			val sign = getSign()

			if (isSignLoaded() && sign == null) {
				IonServer.slF4JLogger.warn("No sign at ticking multiblock! [$x, $y, $z] $facing")



				return
			}

			// If null, its just not loaded
			sign?.let {
				sign.front().line(2, GsonComponentSerializer.gson().deserializeOrNull(string) ?: Component.text("fallback component", PIRATE_DARK_RED))
				Tasks.sync { sign.update() }
			}
		}

		override fun toString(): String {
			return "TestMultiblockEntity[loc = ${Vec3i(x, y, z)}, signOffset = $facing, string = $string]"
		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val origin = getOrigin(sign)
		val (x, y, z) = origin

		val multi = getMultiblockEntity(sign.world, x, y, z)

		if (multi == null) {
			player.userError("NO ENTITY! ORIGIN: $origin")
			return
		}

		multi.string = GsonComponentSerializer.gson().serialize(player.inventory.itemInMainHand.displayName())
		player.success("Set the multiblock test string to ${GsonComponentSerializer.gson().serialize(player.inventory.itemInMainHand.displayName())}, it will be updated next tick")
	}
}
