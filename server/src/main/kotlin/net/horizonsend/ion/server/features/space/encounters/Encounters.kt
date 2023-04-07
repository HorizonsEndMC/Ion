package net.horizonsend.ion.server.features.space.encounters

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.runnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks.syncDelay
import net.starlegacy.util.Tasks.syncRepeat
import net.starlegacy.util.nms
import net.starlegacy.util.spherePoints
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_COW_BELL
import org.bukkit.Particle
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.COW
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.ceil

object Encounters {
	private val encounters: MutableMap<String, Encounter> = mutableMapOf()

	// TODO, test encounter. Will spawn enemies when you open the chest
	@Suppress("Unused")
	val ITS_A_TRAP = register(
		object : Encounter(identifier = "ITS_A_TRAP") {
			override fun generate(chestX: Int, chestY: Int, chestZ: Int) {
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
			override fun generate(chestX: Int, chestY: Int, chestZ: Int) {
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
			override fun generate(chestX: Int, chestY: Int, chestZ: Int) {
				TODO("Not yet implemented")
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				var iteration = 0
				val BLOCKS_PER_ITERATION = 0.10
				val MAX_RADIUS = 100.0

				val iceTypes = WeightedRandomList(
					Material.ICE to 2,
					Material.PACKED_ICE to 4,
					Material.BLUE_ICE to 2,
					Material.PACKED_ICE to 4,
					Material.ICE to 2
				)
				val materialNoise = SimplexOctaveGenerator(chest.world.seed, 1)
				materialNoise.setScale(1.15)

				runnable {
					iteration++

					val currentSize = iteration * BLOCKS_PER_ITERATION
					val currentSizeSquared = currentSize * currentSize

					if (currentSize >= MAX_RADIUS) cancel()

					for (x in (chest.x - ceil(currentSize).toInt())..(chest.x + ceil(currentSize).toInt())) {
						val xSquared = (x - chest.x) * (x - chest.x)

						for (y in (chest.y - ceil(currentSize).toInt())..(chest.y + ceil(currentSize).toInt())) {
							val ySquared = (y - chest.y) * (y - chest.y)
							for (z in (chest.z - ceil(currentSize).toInt())..(chest.z + ceil(currentSize).toInt())) {
								val zSquared = (z - chest.z) * (z - chest.z)

								if (xSquared + ySquared + zSquared > currentSizeSquared) continue

								val block = chest.world.getBlockAt(x, y, z)

								if (block.isEmpty) continue
								if (!block.isSolid) continue
								if (block.type == Material.CHEST) continue
								if (iceTypes.entries().contains(block.type)) continue

								val paletteSample = ((
										materialNoise.noise(
											x.toDouble(),
											y.toDouble(),
											z.toDouble(),
											1.0,
											1.0,
											true
										) + 1
										) / 2
										)

								block.type = iceTypes.getEntry(paletteSample)
							}
						}
					}

					val spherePoints = chest.location.toCenterLocation().spherePoints(currentSize, 500)

					for (player in chest.world.players) {
						if (player.location.distance(chest.location) >= 50.0) continue
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

						player.freezeTicks = player.freezeTicks++
					}
				}.runTaskTimer(IonServer, 0L, 2L)
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
			override fun generate(chestX: Int, chestY: Int, chestZ: Int) {
				TODO("Not yet implemented")
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!
				val timeLimit = 10 // seconds
				event.player.alert("Slaughter the Explosive Cow in $timeLimit seconds or perish!!!")
				val explosiveCow = targetedBlock.location.world.spawnEntity(targetedBlock.location, COW)
				explosiveCow.customName(text("Explosive Cow", NamedTextColor.RED))
				syncDelay((timeLimit * 20).toLong()) {
					if (!explosiveCow.isDead) {
						explosiveCow.location.createExplosion(20.0f)
						val explosionDamage = 25.0
						val explosionRadius = 15.0
						explosiveCow.location.getNearbyLivingEntities(explosionRadius).forEach {
							it.damage(explosionDamage * (explosionRadius - it.location.distance(explosiveCow.location) / explosionRadius), explosiveCow)
						}
					}
				}
				syncRepeat(1, 1) {
					explosiveCow.location.world.playSound(explosiveCow, BLOCK_NOTE_BLOCK_COW_BELL, 5.0f, 1.0f)
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
	private fun <T : Encounter> register(encounter: T): T {
		encounters[encounter.identifier] = encounter
		return encounter
	}

	val identifiers = encounters.keys

	operator fun get(identifier: String): Encounter? = encounters[identifier]

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

	fun setChestLock(chest: Chest, locked: Boolean) {
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
			wreck.putBoolean("inactive", locked)
			existingWrecks.add(wreck)
		}

		wreckData.put("Wrecks", existingWrecks)

		setChunkEncounters(chest.chunk, wreckData)
	}

	operator fun get(chest: Chest): Encounter? {
		val wreckData = getChunkEncounters(chest.chunk) ?: return null

		val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)
		for (wreck in existingWrecks) {
			wreck as CompoundTag

			if (!encounterMatchesChest(wreck, chest)) continue

			return get(wreck.getString("Encounter Identifier"))
		}

		return null
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

	open fun generate(chestX: Int, chestY: Int, chestZ: Int) {}
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
