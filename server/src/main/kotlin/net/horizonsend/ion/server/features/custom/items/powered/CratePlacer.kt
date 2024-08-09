package net.horizonsend.ion.server.features.custom.items.powered

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentManager.getShipmentItemId
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
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
import org.bukkit.craftbukkit.v1_20_R3.block.CraftShulkerBox
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType.STRING

object CratePlacer : CustomItem("CRATE_PLACER"), PoweredItem, CustomModeledItem {
	val displayName: Component = ofChildren(text("Crate ", GOLD), text("Placer", GRAY)).decoration(ITALIC, false)

	override val material: Material = Material.DIAMOND_PICKAXE
	override val customModelData: Int = 10

	override val displayDurability: Boolean = true

	override fun getPowerCapacity(itemStack: ItemStack): Int = 50_000
	override fun getPowerUse(itemStack: ItemStack): Int = 10

	override fun getLoreManagers(): List<LoreCustomItem.CustomItemLoreManager> {
		return listOf(PoweredItem.PowerLoreManager)
	}

	override fun constructItemStack(): ItemStack {
		val base = getModeledItem()

		setPower(base, getPowerCapacity(base))

		return base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}
	}

	val range = 16

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity !is Player) return
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown
		if (getPower(itemStack) < getPowerUse(itemStack)) return

		fireLaser(livingEntity)
		val lookingAt = livingEntity.getTargetBlockExact(range) ?: return

		nearTargets(lookingAt).forEach { pair ->
			placeCrate(livingEntity, itemStack, pair.first, pair.second, pair.third)
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

			val itemNBT = CompoundTag()
			nms.save(itemNBT)

			val boxEntity = target.state as ShulkerBox
			boxEntity.customName = item.itemMeta.displayName
			boxEntity.update()

			// Add the raw nms tag for shipment id
			val id = getShipmentItemId(item)
			val entity = (target.state as CraftShulkerBox).tileEntity
			val chunk = entity.location.chunk.minecraft
			// Save the full compound tag
			val base = entity.saveWithFullMetadata()

			//incomplete crates dont have shipment ids
			if (id != null) base.put("shipment_oid", StringTag.valueOf(id))

			val items = ListTag()
			items.add(itemNBT)

			base.put("Items", items)

			println(NbtUtils.structureToSnbt(base))

			val blockPos = BlockPos(x, y, z)
			// Remove old
			chunk.removeBlockEntity(blockPos)

			val blockEntity = BlockEntity.loadStatic(blockPos, entity.blockState, base)!!
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

				removePower(itemStack, getPowerUse(itemStack))

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
			range.toDouble(),
			FluidCollisionMode.NEVER,
			true,
			0.1,
		) { false }

		val end: Location = raytrace?.hitPosition?.toLocation(livingEntity.world) ?: livingEntity.eyeLocation.clone().add(start.direction.clone().multiply(range))

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
