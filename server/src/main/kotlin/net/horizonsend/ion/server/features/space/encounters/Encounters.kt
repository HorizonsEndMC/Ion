package net.horizonsend.ion.server.features.space.encounters

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.highlightBlock
import net.horizonsend.ion.server.miscellaneous.runnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.BlockPos
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks.syncDelay
import net.starlegacy.util.Tasks.syncRepeat
import net.starlegacy.util.nms
import net.starlegacy.util.spherePoints
import net.starlegacy.util.toBlockPos
import net.starlegacy.util.toLocation
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_COW_BELL
import org.bukkit.Particle
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_HARP
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.COW
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.ceil

object Encounters {
	private val encounters: MutableMap<String, Encounter> = mutableMapOf()

	// TODO, test encounter. Will spawn enemies when you open the chest
	@Suppress("Unused")
	val ITS_A_TRAP = register(
		object : Encounter(identifier = "ITS_A_TRAP") {
			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {
				TODO("Not yet implemented")
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!
				event.player.alert("it worked")
				for (count in 0..100) {
					targetedBlock.location.world.spawnEntity(targetedBlock.location, EntityType.LIGHTNING)
				}
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "minecraft:chests/abandoned_mineshaft")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)

	@Suppress("Unused")
	val TIC_TAC_TOE = register(
		object : Encounter(identifier = "TIC_TAC_TOE") {
			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {
				TODO("Not yet implemented")
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				//True for player, false for ai
				var whoTurn: Boolean = true
				val targetedBlock = event.clickedBlock as? Chest ?: return
				val lock = targetedBlock.persistentDataContainer.get(NamespacedKeys.WRECK_CHEST_LOCK, PersistentDataType.INTEGER)
				val player = event.player
				if (lock == 1) {
					MenuHelper.apply {
						MenuHelper
						this.gui(5, "Tic tac toe")
						val unclaimedButton = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
						//player marker
						val redClaimedButton = ItemStack(Material.RED_STAINED_GLASS_PANE)
						//ai marker
						val blueClaimedButton = ItemStack(Material.BLUE_STAINED_GLASS_PANE)
						val indexesOfTheButtons = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
						val items = mutableListOf<GuiItem>()
						indexesOfTheButtons.forEach { index ->
							items[index] = guiButton(unclaimedButton) {
								fun checkHasWon(): Boolean? {
									//Win condition of the top row
									if ((items[12].item.type == items[13].item.type && items[13].item.type == items[14].item.type)) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition of the Middle row
									if (items[21].item.type == items[22].item.type && items[22].item.type == items[23].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition of the bottom row
									if (items[30].item.type == items[31].item.type && items[31].item.type == items[32].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition for the leftmost column
									if (items[12].item.type == items[21].item.type && items[21].item.type == items[30].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition for the middle column
									if (items[13].item.type == items[22].item.type && items[22].item.type == items[31].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition for the rightmost column
									if (items[14].item.type == items[23].item.type && items[23].item.type == items[32].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition for the right diagonal
									if (items[12].item.type == items[22].item.type && items[22].item.type == items[32].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									//Win condition for the left diagonal
									if (items[14].item.type == items[22].item.type && items[22].item.type == items[30].item.type) {
										return when (items[14].item.type) {
											Material.RED_STAINED_GLASS_PANE -> true
											Material.BLUE_STAINED_GLASS_PANE -> false
											else -> null
										}
									}
									return null
								}
								if (items[index] != blueClaimedButton && whoTurn) {
									items[index] = guiButton(redClaimedButton) {}
									if (checkHasWon() == null) {
										whoTurn = !whoTurn
									} else if (checkHasWon() == true) {
										targetedBlock.persistentDataContainer.set(
											NamespacedKeys.WRECK_CHEST_LOCK,
											PersistentDataType.SHORT,
											0
										)
									}
									//AI below (highly advanced, with 100% quantum neural deeplearning frameworks)
									if (!whoTurn) {
										val freeButtons =
											indexesOfTheButtons.filter { items[it].item == unclaimedButton }
										if (freeButtons.isNotEmpty()) {
											items[freeButtons.random()] = guiButton(blueClaimedButton) {}
											whoTurn = !whoTurn
											checkHasWon()
										}
									} else {
										targetedBlock.persistentDataContainer.set(
											NamespacedKeys.WRECK_CHEST_LOCK,
											PersistentDataType.SHORT,
											0
										)
									}
								}
							}
						}
						player.openPaginatedMenu("Tic Tic Toe", items)
					}
				}
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "minecraft:chests/abandoned_mineshaft")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)

	@Suppress("Unused")
	val COOLANT_LEAK = register(object : Encounter(identifier = "COOLANT_LEAK") {
			fun getBlocks(world: World, origin: BlockPos, radius: Double): List<Block> {
				val radiusSquared = radius * radius

				val blocks = mutableListOf<Block>()

				for (x in (origin.x - ceil(radius).toInt())..(origin.x + ceil(radius).toInt())) {
					val xSquared = (x - origin.x) * (x - origin.x)

					for (y in (origin.y - ceil(radius).toInt())..(origin.y + ceil(radius).toInt())) {
						val ySquared = (y - origin.y) * (y - origin.y)
						for (z in (origin.z - ceil(radius).toInt())..(origin.z + ceil(radius).toInt())) {
							val zSquared = (z - origin.z) * (z - origin.z)

							if (xSquared + ySquared + zSquared > radiusSquared) continue

							blocks += world.getBlockAt(x, y, z)
						}
					}
				}

				return blocks
			}

			fun getLever(chest: Chest): BlockPos? {
				val wreckData = getChunkEncounters(chest.chunk) ?: return null

				val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)

				val encounter = existingWrecks.first { wreck ->
					wreck as CompoundTag

					return@first (wreck.getInt("x") == chest.x &&
							wreck.getInt("y") == chest.y &&
							wreck.getInt("z") == chest.z)
				} as CompoundTag

				return BlockPos(
					encounter.getInt("leverPosX"),
					encounter.getInt("leverPosY"),
					encounter.getInt("leverPosZ")
				)
			}

			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {
				val chestPos = BlockPos(chestX, chestY, chestZ)
				val surroundingBlocks = getBlocks(world, chestPos, 8.0)

				fun checkAir(block: Block): Boolean {
					val up1 = block.getRelative(BlockFace.UP)
					val up2 = up1.getRelative(BlockFace.UP)

					return up1.isEmpty && up2.isEmpty
				}

				val leverOn = surroundingBlocks.filter { checkAir(it) && it.isSolid }.random()
				leverOn.type = Material.REINFORCED_DEEPSLATE
				val leverBlock = leverOn.getRelative(BlockFace.UP)

				val blockData = (Material.LEVER.createBlockData() as Switch)
				blockData.attachedFace = FaceAttachable.AttachedFace.FLOOR

				leverBlock.blockData = blockData

				val chestChunk = world.getChunkAt(chestPos.toLocation(world))

				val wreckData = getChunkEncounters(chestChunk) ?: return

				val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)
				wreckData.remove("Wrecks")

				for (wreck in existingWrecks) {
					wreck as CompoundTag

					if (
						wreck.getInt("x") != chestPos.x &&
						wreck.getInt("y") != chestPos.y &&
						wreck.getInt("z") != chestPos.z
					) continue

					existingWrecks.remove(wreck)
					wreck.putInt("leverPosX", leverBlock.x)
					wreck.putInt("leverPosY", leverBlock.y)
					wreck.putInt("leverPosZ", leverBlock.z)
					existingWrecks.add(wreck)
				}

				wreckData.put("Wrecks", existingWrecks)

				setChunkEncounters(chestChunk, wreckData)
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				setChestFlag(chest, "locked", ByteTag.valueOf(true))

				var iteration = 0
				val BLOCKS_PER_ITERATION = 0.10
				val MAX_RADIUS = 15.0

				val leverPos = getLever(chest)

				getLever(chest)?.let { highlightBlock(event.player, it.below()) }

				val iceTypes = listOf(
					Material.ICE,
					Material.PACKED_ICE,
					Material.BLUE_ICE,
					Material.PACKED_ICE,
					Material.ICE,
				)

				var attempts = 0

				leverPos?.let {
					runnable {
						val currentSize = iteration * BLOCKS_PER_ITERATION

						if (attempts > 500) cancel()
						attempts++

						val leverState = chest.world.getBlockAt(leverPos.x, leverPos.y, leverPos.z).state
						if ((leverState.blockData as Switch).isPowered) {
							setChestFlag(chest, "locked", ByteTag.valueOf(false))
							setChestFlag(chest, "inactive", ByteTag.valueOf(true))
							cancel()
						}

						for (block in getBlocks(
							chest.world,
							chest.location.toCenterLocation().toBlockPos(),
							currentSize
						)) {
							if (block.isEmpty) continue
							if (!block.isSolid) continue
							if (block.type == Material.CHEST) continue
							if (iceTypes.contains(block.type)) continue

							block.type = iceTypes.random()

						}

						if (currentSize <= MAX_RADIUS) iteration++

						val spherePoints = chest.location.toCenterLocation().spherePoints(currentSize, 500)

						for (player in chest.world.players) {
							if (player.location.distance(chest.location) >= maxOf(currentSize, 100.0)) continue
							for (spherePoint in spherePoints) {

								player.spawnParticle(
									Particle.SNOWFLAKE,
									spherePoint.x,
									spherePoint.y,
									spherePoint.z,
									1,
									0.0,
									0.0,
									0.0,
									0.1,
									null
								)
							}

							if (player.location.distance(chest.location) >= currentSize) continue

							player.freezeTicks = player.freezeTicks + 10
						}
					}.runTaskTimer(IonServer, 0L, 2L)
				}
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "minecraft:chests/abandoned_mineshaft")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)

	@Suppress("Unused")
	val COW_TIPPER = register(
		object : Encounter(identifier = "COW_TIPPER") {
			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {
				TODO("Not yet implemented")
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				if (getChestFlag(chest, "locked") as? ByteTag == ByteTag.valueOf(true))
					return

				setChestFlag(chest, "locked", ByteTag.valueOf(true))

				val explosiveCow = targetedBlock.location.world.spawnEntity(targetedBlock.location, COW)
				explosiveCow.customName(text("Explosive Cow", NamedTextColor.RED))
				val timeLimit = 15 // seconds
				var iteration = 0 // ticks
				event.player.alert("Slaughter the Explosive Cow in $timeLimit seconds or perish!!!")

				runnable {
					if (iteration % 5 == 0) {
						explosiveCow.location.world.playSound(explosiveCow, BLOCK_NOTE_BLOCK_COW_BELL, 5.0f, 1.0f)
					}
					if (iteration >= timeLimit * 20 - 100) {
						explosiveCow.location.world.playSound(explosiveCow, BLOCK_NOTE_BLOCK_COW_BELL, 5.0f, 1.0f)
					}
					if (explosiveCow.isDead) {
						setChestFlag(chest, "locked", ByteTag.valueOf(false))
						setChestFlag(chest, "inactive", ByteTag.valueOf(true))
						event.player.information("The chest was unlocked.")
						cancel()
					}
					if (timeLimit * 20 == iteration) {
						explosiveCow.location.createExplosion(30.0f)
						val explosionDamage = 25.0
						val explosionRadius = 15.0
						explosiveCow.location.getNearbyLivingEntities(explosionRadius).forEach {
							it.damage(explosionDamage * (explosionRadius - it.location.distance(targetedBlock.location) / explosionRadius), explosiveCow)
						}
						setChestFlag(chest, "locked", ByteTag.valueOf(false))
						event.player.userError("You were tipped by the Explosive Cow!")
						cancel()
					}
					iteration++
				}.runTaskTimer(IonServer, 0L, 1L)
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "minecraft:chests/abandoned_mineshaft")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)


	@Suppress("Unused")
	val TIMED_BOMB = register(
		object : Encounter(identifier = "TIMED_BOMB") {
			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {
				TODO("Not yet implemented")
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!
				val chest = (targetedBlock.state as? Chest) ?: return
				setChestFlag(chest, "locked", ByteTag.valueOf(false))
				setChestFlag(chest, "inactive", ByteTag.valueOf(true))

				val timeLimit = 30 // seconds
				var iteration = 0 // ticks
				event.player.alert("Timed bomb activated! Loot the wreck and get out in $timeLimit seconds before the explosion!")
				runnable {
					if (iteration % 5 == 0) {
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_HARP, 5.0f, 2.0f)
					}
					if (iteration >= timeLimit * 20 - 100) {
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_HARP, 5.0f, 2.0f)
					}
					if (timeLimit * 20 == iteration) {
						val explosionRadius = 10.0 // For spawning actual explosions
						val explosionDamage = 30.0
						val explosionDamageRadius = 20.0 // For entity damage calculation
						targetedBlock.location.spherePoints(explosionRadius, 10).forEach {
							it.createExplosion(10.0f)
						}
						targetedBlock.location.getNearbyLivingEntities(explosionDamageRadius).forEach {
							if (it.location.distance(targetedBlock.location) == 0.0) it.damage(explosionDamage)
							else it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(targetedBlock.location)) / explosionDamageRadius)
						}
						cancel()
					}
					iteration++
				}.runTaskTimer(IonServer, 0L, 1L)
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "minecraft:chests/abandoned_mineshaft")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)

	private fun <T : Encounter> register(encounter: T): T {
		encounters[encounter.identifier] = encounter
		return encounter
	}

	val identifiers = encounters.keys

	operator fun get(identifier: String): Encounter? = encounters[identifier]
	operator fun get(serialzedEncounter: CompoundTag): Encounter? {
		val identifier = serialzedEncounter.getString("Encounter Identifier")

		return encounters[identifier]
	}

	operator fun get(chest: Chest): Encounter? {
		val wreckData = getChunkEncounters(chest.chunk) ?: return null

		val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)
		for (wreck in existingWrecks) {
			wreck as CompoundTag

			if (!encounterMatchesChest(wreck, chest)) continue
			if (wreck.getBoolean("inactive")) continue

			return get(wreck.getString("Encounter Identifier"))
		}

		return null
	}

	fun getChunkEncounters(chunk: Chunk): CompoundTag? {
		val pdc = chunk.persistentDataContainer.get(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY
		) ?: return null

		return NbtIo.readCompressed(
			ByteArrayInputStream(
				pdc,
				0,
				pdc.size
			)
		)
	}

	fun setChunkEncounters(chunk: Chunk, newData: CompoundTag) {
		val wreckDataOutputStream = ByteArrayOutputStream()
		NbtIo.writeCompressed(newData, wreckDataOutputStream)

		chunk.persistentDataContainer.set(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY,
			wreckDataOutputStream.toByteArray()
		)
	}

	fun getChestFlag(chest: Chest, key: String) : Tag? {
		val wreckData = getChunkEncounters(chest.chunk) ?: return null

		val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)

		for (wreck in existingWrecks) {
			wreck as CompoundTag

			if (
				wreck.getInt("x") != chest.x &&
				wreck.getInt("y") != chest.y &&
				wreck.getInt("z") != chest.z
			) continue

			return wreck.get(key)
		}

		return null
	}

	fun setChestFlag(chest: Chest, key: String, tag: Tag) {
		val wreckData = getChunkEncounters(chest.chunk) ?: return

		val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)
		wreckData.remove("Wrecks")

		for (wreck in existingWrecks) {
			wreck as CompoundTag

			if (
				wreck.getInt("x") != chest.x &&
				wreck.getInt("y") != chest.y &&
				wreck.getInt("z") != chest.z
			) continue

			existingWrecks.remove(wreck)
			wreck.remove(key)
			wreck.put(key, tag)
			existingWrecks.add(wreck)
		}

		wreckData.put("Wrecks", existingWrecks)

		setChunkEncounters(chest.chunk, wreckData)
	}

	fun encounterMatchesChest(wreck: CompoundTag, chest: Chest): Boolean = (
			wreck.getInt("x") == chest.x &&
			wreck.getInt("y") == chest.y &&
			wreck.getInt("z") == chest.z)
}

/**
 * A basic class controlling an encounter on a wreck.
 *
 * @property constructChestState Code used when generating the primary chest on the wreck.
 * 	This is executed when it places the chest block.
 * @property onChestInteract Code executed when the primary chest is interacted with.
 * @property generate Additional instructions executed when generating the wreck.
 **/
abstract class Encounter(
	val identifier: String
) {
	open fun constructChestState(): Pair<BlockState, CompoundTag?> {
		return Material.CHEST.createBlockData().nms to null
	}

	open fun onChestInteract(event: PlayerInteractEvent) {}

	open fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {}
}

enum class SecondaryChests(val blockState: BlockState, val NBT: CompoundTag?, val money: Int?) {
	REPAIR_MATERIALS(Blocks.CHEST.defaultBlockState(), null, 500),
	FOOD(Blocks.CHEST.defaultBlockState(), null, 500),
	GUN_PARTS(Blocks.CHEST.defaultBlockState(), null, 500),
	POWER_ARMOR_MODS(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_POOR(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_GOOD(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_GREAT(Blocks.CHEST.defaultBlockState(), null, 500);

	companion object {
		private val map = SecondaryChests.values().associateBy { it.name }

		operator fun get(value: String): SecondaryChests? = map[value]
	}
}
