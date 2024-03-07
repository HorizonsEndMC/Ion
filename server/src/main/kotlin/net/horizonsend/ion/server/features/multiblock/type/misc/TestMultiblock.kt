package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATE_DARK_RED
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
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

	override fun createEntity(data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int): TestMultiblockEntity {
		return TestMultiblockEntity(world, x, y, z, data.getAdditionalData(NamespacedKeys.key("test"), PersistentDataType.STRING) ?: "")
	}

	class TestMultiblockEntity(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		var string: String
	) : MultiblockEntity(x, y, z, world, TestMultiblock) {
		override fun storeAdditionalData(store: PersistentMultiblockData) {
			store.addAdditionalData(NamespacedKeys.key("test"), PersistentDataType.STRING, string)
		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		sign.line(2, player.inventory.itemInMainHand.displayName())

		val origin = Vec3i(sign.location).minus(Vec3i(sign.getFacing().modX, 0, sign.getFacing().modZ))
		val (x, y, z) = origin

		val multi = getMultiblockEntity(sign.world, x, y, z)

		if (multi == null) {
			player.userError("NO ENTITY! ORIGIN: $origin")
			return
		}

		multi.string = player.inventory.itemInMainHand.displayName().plainText()

		sign.update()
	}
}
