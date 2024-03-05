package net.horizonsend.ion.server.features.custom.items.type.tool

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentManager.getShipmentItemId
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.minecraft.core.BlockPos
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.ShulkerBox
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Piston
import org.bukkit.craftbukkit.block.CraftShulkerBox
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

object CratePlacer : CustomItem(
	"CRATE_PLACER",
	ofChildren(text("Crate ", GOLD), text("Placer", GRAY)).itemName,
	ItemFactory
		.builder()
		.setMaterial(Material.DIAMOND_PICKAXE)
		.setCustomModel("tool/crate_placer")
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.POWER_STORAGE, PowerStorage(50_000, 10, true))
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@CratePlacer) { event, _, item ->
			tryPlaceCrate(event.player, item)
		})
	}

	private const val RANGE = 16

	private fun tryPlaceCrate(player: Player, itemStack: ItemStack) {
		if (player.hasCooldown(itemStack.type)) return // Cooldown
		val powerManager = getComponent(CustomComponentTypes.POWER_STORAGE)
		if (powerManager.getPower(itemStack) < powerManager.getPowerUse(itemStack, this)) return

		fireLaser(player)
		val lookingAt = player.getTargetBlockExact(RANGE) ?: return

		nearTargets(lookingAt).forEach { pair ->
			placeCrate(player, itemStack, pair.first, pair.second, pair.third)
		}
	}

	private fun placeCrate(player: Player, itemStack: ItemStack, target: Block, against: Block, offset: BlockFace) {
		val x = target.x
		val y = target.y
		val z = target.z

		val state = target.state //current listeners dont seem to use this... hopefully
		val tempData = target.blockData.clone() // save the data for failures

		for (item in player.inventory) {
			if (item == null) continue

			if (!item.type.isShulkerBox) continue
			if (CargoCrates[item] == null) continue

			val itemState = (item.itemMeta as BlockStateMeta).blockState as ShulkerBox
			//attempt to place the crate
			//I copied gutins code and prayed that it worked
			//fake block place event
			val data = item.type.createBlockData()
			data as Directional

			data.facing = offset

			target.setBlockData(data, true)

			val paperItem = itemState.inventory.filterNotNull().first()
			val nms = CraftItemStack.asNMSCopy(paperItem)

			val itemNBT = nms.save(player.world.minecraft.registryAccess())

			val boxEntity = target.state as ShulkerBox
			boxEntity.customName(item.itemMeta.displayName())
			boxEntity.update()

			// Add the raw nms tag for shipment id
			val id = getShipmentItemId(item)
			val entity = (target.state as CraftShulkerBox).tileEntity
			val chunk = entity.location.chunk.minecraft
			// Save the full compound tag
			val base = entity.saveWithFullMetadata(player.world.minecraft.registryAccess())

			//incomplete crates dont have shipment ids
			if (id != null) base.put("shipment_oid", StringTag.valueOf(id))

			val items = ListTag()
			items.add(itemNBT)

			base.put("Items", items)

			val blockPos = BlockPos(x, y, z)
			// Remove old
			chunk.removeBlockEntity(blockPos)

			val blockEntity = BlockEntity.loadStatic(blockPos, entity.blockState, base, player.world.minecraft.registryAccess())!!
			chunk.addAndRegisterBlockEntity(blockEntity)

			//event check
			val event = BlockPlaceEvent(
				target,
				state,
				against,
				item,
				player,
				true,
				EquipmentSlot.HAND
			)

			if (event.callEvent()) {
				player.inventory.removeItem(item.asOne())

				val powerManager = getComponent(CustomComponentTypes.POWER_STORAGE)
				powerManager.removePower(itemStack, this, powerManager.getPowerUse(itemStack, this))

				target.world.playSound(
					target.location,
					"minecraft:block.stone.place",
					SoundCategory.PLAYERS,
					1.0f,
					1.0f
				)

				target.world.playSound(
					target.location,
					"minecraft:block.honey_block.slide",
					SoundCategory.PLAYERS,
					1.0f,
					1.0f
				)

				break
			} else {
				//placement is invalid, revert back to old state
				target.setBlockData(tempData, true)
				break
			}
		}
	}

	private fun fireLaser(livingEntity: LivingEntity) {
		val start = livingEntity.eyeLocation.clone()
		start.y -= 0.15

		val raytrace = livingEntity.world.rayTrace(
			livingEntity.eyeLocation,
			start.direction.clone(),
			RANGE.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			0.1,
		) { false }

		val end: Location = raytrace?.hitPosition?.toLocation(livingEntity.world) ?: livingEntity.eyeLocation.clone().add(start.direction.clone().multiply(RANGE))

		Laser.GuardianLaser(end, start, 5, -1).durationInTicks().start(IonServer)

		start.world.playSound(
			start,
			"minecraft:entity.guardian.attack",
			SoundCategory.PLAYERS,
			1.0f,
			1.0f
		)
	}

	private fun nearTargets(target : Block) : List<Triple<Block, Block, BlockFace>>{
		val nearblocks :MutableList<Block> = mutableListOf()

		for (x in -1..1) for (y in -1..1) for (z in -1..1) {
			val relative = target.getRelative(x, y, z)
			if (relative == target) continue

			// if you have lava on your ship I will judge you
			if (relative.type == Material.AIR || relative.type == Material.WATER) nearblocks.add(relative)
		}

		val adjStikies : MutableList<Pair<Block, BlockFace>> = mutableListOf()
		val filtered = nearblocks.filter {
			var valid = false

			for (face in ADJACENT_BLOCK_FACES) {
				val adj = it.getRelative(face)

				if (adj.type == Material.STICKY_PISTON) {
					valid = true

					val data = adj.blockData as Piston

					if (data.facing == face.oppositeFace) {
						adjStikies.add(adj to face.oppositeFace)
					} else {
						valid = false
					}

					break
				}
			}

			valid
		}

		return filtered.zip(adjStikies).map { (target, against) ->
			Triple(target, against.first, against.second)
		}
	}
}
