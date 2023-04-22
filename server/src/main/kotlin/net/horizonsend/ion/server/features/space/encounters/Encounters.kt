package net.horizonsend.ion.server.features.space.encounters

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import fr.skytasul.guardianbeam.Laser.GuardianLaser
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.customitems.CustomItems.PISTOL
import net.horizonsend.ion.server.features.space.encounters.Encounters.createLootChest
import net.horizonsend.ion.server.features.space.generation.BlockSerialization.readChunkCompoundTag
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.castSpawnEntity
import net.horizonsend.ion.server.miscellaneous.highlightBlock
import net.horizonsend.ion.server.miscellaneous.runnable
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.BlockPos
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.feature.nations.gui.skullItem
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.distance
import net.starlegacy.util.nms
import net.starlegacy.util.randomInt
import net.starlegacy.util.spherePoints
import net.starlegacy.util.toBlockPos
import net.starlegacy.util.toLocation
import net.starlegacy.util.updateMeta
import org.bukkit.Chunk
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.BLUE_GLAZED_TERRACOTTA
import org.bukkit.Material.CHEST
import org.bukkit.Material.MAGENTA_GLAZED_TERRACOTTA
import org.bukkit.Material.ORANGE_GLAZED_TERRACOTTA
import org.bukkit.Material.RED_GLAZED_TERRACOTTA
import org.bukkit.Material.REINFORCED_DEEPSLATE
import org.bukkit.Material.STONE_BUTTON
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_COW_BELL
import org.bukkit.Particle
import org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_HARP
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_SNARE
import org.bukkit.Sound.ENTITY_WARDEN_SONIC_CHARGE
import org.bukkit.Sound.ITEM_FLINTANDSTEEL_USE
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.block.data.FaceAttachable.AttachedFace.FLOOR
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.COW
import org.bukkit.entity.EntityType.ENDERMITE
import org.bukkit.entity.EntityType.SKELETON
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.ShulkerBullet
import org.bukkit.entity.Skeleton
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.UUID
import kotlin.math.ceil

object Encounters {
	private val encounters: MutableMap<String, Encounter> = mutableMapOf()

	// test encounter. Will spawn enemies when you open the chest
	@Suppress("Unused")
	val ITS_A_TRAP = register(
		object : Encounter(identifier = "ITS_A_TRAP") {
			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {}

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
			fun getLever(chest: Chest): BlockPos? {
				val wreckData = readChunkCompoundTag(chest.chunk, NamespacedKeys.WRECK_ENCOUNTER_DATA) ?: return null

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

			fun placeLever(chest: Chest) {
				val chestPos = BlockPos(chest.x, chest.y, chest.z)

				fun checkAir(block: Block): Boolean {
					val up1 = block.getRelative(UP)
					val up2 = up1.getRelative(UP)

					return up1.isEmpty && up2.isEmpty
				}

				val surroundingBlocks = getBlocks(chest.world, chestPos, 8.0) {
					checkAir(it) && it.isSolid && it.type != CHEST
				}

				val leverOn = surroundingBlocks.random()
				leverOn.type = REINFORCED_DEEPSLATE
				val leverBlock = leverOn.getRelative(UP)

				val blockData = (Material.LEVER.createBlockData() as Switch)
				blockData.attachedFace = FLOOR

				leverBlock.blockData = blockData

				val chestChunk = chest.world.getChunkAt(chestPos.toLocation(chest.world))

				val wreckData = readChunkCompoundTag(chestChunk, NamespacedKeys.WRECK_ENCOUNTER_DATA) ?: return

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

				placeLever(chest)
				setChestFlag(chest, "locked", ByteTag.valueOf(true))

				var iteration = 0
				val BLOCKS_PER_ITERATION = 0.10
				val MAX_RADIUS = 15.0
				val MAX_ATTEMPTS = 500

				val leverPos = getLever(chest)

				getLever(chest)?.let { highlightBlock(event.player, it.below(), (MAX_ATTEMPTS * 2).toLong()) }

				val iceTypes = listOf(
					Material.ICE,
					Material.PACKED_ICE,
					Material.BLUE_ICE,
					Material.PACKED_ICE,
					Material.ICE,
				)

				var attempts = 0

				event.player.alert("The chest triggered a coolant leak! Find the lever to stop the leak!")
				targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_FIRE_EXTINGUISH, 5.0f, 0.0f)

				leverPos?.let {
					runnable {
						val currentSize = iteration * BLOCKS_PER_ITERATION

						if (attempts > MAX_ATTEMPTS) {
							event.player.userError("Coolant leak expired.")
							cancel()
						}
						attempts++

						val leverState = chest.world.getBlockAt(leverPos.x, leverPos.y, leverPos.z).state
						if ((leverState.blockData as Switch).isPowered) {
							setChestFlag(chest, "locked", ByteTag.valueOf(false))
							setChestFlag(chest, "inactive", ByteTag.valueOf(true))
							event.player.success("Coolant leak deactivated! The chest is now unlocked.")
							cancel()
						}

						for (block in getBlocks(
							chest.world,
							chest.location.toCenterLocation().toBlockPos(),
							currentSize
						) {
							!it.isEmpty && it.isSolid && !iceTypes.contains(it.type) && it.type != CHEST
						} ) {
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
			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				if (getChestFlag(chest, "locked") as? ByteTag == ByteTag.valueOf(true)) {
					event.player.userError("You must slaughter the Explosive Cow before opening the chest!")
					return
				}

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
						event.player.success("The Explosive Cow was put down! The chest is now unlocked.")
						cancel()
					}
					if (timeLimit * 20 == iteration) {
						val explosionRadius = 7.5 // For spawning actual explosions
						val explosionDamage = 40.0
						val explosionDamageRadius = 15.0 // For entity damage calculation
						explosiveCow.location.spherePoints(explosionRadius, 10).forEach {
							it.createExplosion(7.5f)
						}
						explosiveCow.location.getNearbyLivingEntities(explosionRadius).forEach {
							it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(explosiveCow.location)) / explosionDamageRadius, explosiveCow)
						}
						if (!explosiveCow.isDead) {
							(explosiveCow as LivingEntity).damage(explosionDamage)
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
	val INFESTED = register(
		object : Encounter(identifier = "INFESTED") {
			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				if (getChestFlag(chest, "locked") as? ByteTag == ByteTag.valueOf(true)) {
					event.player.userError("You must dispatch the Endermites before opening the chest!")
					return
				}

				setChestFlag(chest, "locked", ByteTag.valueOf(true))

				val endermites = mutableListOf<Entity>()
				for (i in 0..4) {
					val endermite = targetedBlock.location.world.spawnEntity(targetedBlock.getRelative(UP).location, ENDERMITE)
					endermites.add(endermite)
				}

				val timeLimit = 20 // seconds
				var iteration = 0 // ticks
				event.player.alert("Seems like this chest is infested with Endermites! Eliminate the pests in $timeLimit seconds!")

				runnable {
					if (endermites.all { it.isDead } ) {
						setChestFlag(chest, "locked", ByteTag.valueOf(false))
						setChestFlag(chest, "inactive", ByteTag.valueOf(true))
						event.player.success("The infestation was removed and the chest is now unlocked.")
						cancel()
					}
					if (timeLimit * 20 == iteration || event.player.isDead) {
						endermites.forEach { it.remove() }
						setChestFlag(chest, "locked", ByteTag.valueOf(false))
						event.player.userError("You could not remove the infestation! The Endermites have returned to the chest.")
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
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
					}
					if (iteration >= timeLimit * 20 - 100) {
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
					}
					if (timeLimit * 20 == iteration) {
						val explosionRadius = 10.0 // For spawning actual explosions
						val explosionDamage = 50.0
						val explosionDamageRadius = 20.0 // For entity damage calculation
						targetedBlock.location.spherePoints(explosionRadius, 10).forEach {
							it.createExplosion(10.0f)
						}
						targetedBlock.location.getNearbyLivingEntities(explosionDamageRadius).forEach {
							it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(targetedBlock.location)) / explosionDamageRadius)
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

	@Suppress("Unused")
	val DEFUSE_BOMB = register(
		object : Encounter(identifier = "DEFUSE_BOMB") {

			val validColors = listOf(ORANGE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA)
			val displayColorMap = mapOf(
				ORANGE_GLAZED_TERRACOTTA to "<#eb9111>Orange Stripes",
				BLUE_GLAZED_TERRACOTTA to "<#3c44aa>Blue Gem",
				RED_GLAZED_TERRACOTTA to "<#b82f27>Red Swirl",
				MAGENTA_GLAZED_TERRACOTTA to "<#d460cf>Magenta Arrow"
			)

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				if (getChestFlag(chest, "locked") as? ByteTag == ByteTag.valueOf(true)) {
					event.player.userError("You must defuse the bomb before opening the chest!")
					return
				}

				setChestFlag(chest, "locked", ByteTag.valueOf(true))

				val timeLimit = 60 // seconds
				var iteration = 0 // ticks
				event.player.alert("Defusable bomb activated! Press the buttons in the correct order within $timeLimit seconds!")

				val surroundingBlocks = getBlocks(chest.world, chest.location.toBlockPos(), 10.0) {
					checkAir(it) && it.isSolid && it.type != CHEST && it.type !in validColors
				}

				val buttonList = mutableListOf<Block>()

				// Button placer
				for (color in validColors) {
					val buttonOn = surroundingBlocks.random()
					buttonOn.type = color
					val buttonBlock = buttonOn.getRelative(UP)

					val blockData = (STONE_BUTTON.createBlockData() as Switch)
					blockData.attachedFace = FLOOR

					buttonBlock.blockData = blockData
					buttonList.add(buttonBlock)
					highlightBlock(event.player, buttonOn.location.toBlockPos(), (timeLimit * 20).toLong())
				}

				val correctOrder = validColors.shuffled()
				val selected = mutableListOf<Material>()
				var failed = false
				event.player.information(
					"Search for colored buttons in the wreck and press them in the right order:\n" +
							"  <gray>1: ${displayColorMap[correctOrder[0]]}\n" +
							"  <gray>2: ${displayColorMap[correctOrder[1]]}\n" +
							"  <gray>3: ${displayColorMap[correctOrder[2]]}\n" +
							"  <gray>4: ${displayColorMap[correctOrder[3]]}"
				)
				event.player.information("Do not attempt to break the buttons!")

				runnable {
					// timer sounds
					if (iteration % 20 == 0) {
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
					}
					if (iteration >= timeLimit * 20 - 300 && iteration % 5 == 0) { // 15 seconds left
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
					}
					if (iteration >= timeLimit * 20 - 100) { // 5 seconds left
						targetedBlock.location.world.playSound(targetedBlock.location, BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
					}

					// explosion
					if (timeLimit * 20 == iteration) {
						val explosionRadius = 15.0 // For spawning actual explosions
						val explosionDamage = 100.0
						val explosionDamageRadius = 30.0 // For entity damage calculation
						targetedBlock.location.spherePoints(explosionRadius / 2, 10).forEach {
							it.createExplosion(10.0f) // inner explosion
						}
						targetedBlock.location.spherePoints(explosionRadius, 20).forEach {
							it.createExplosion(15.0f) // outer explosion
						}
						targetedBlock.location.getNearbyLivingEntities(explosionDamageRadius).forEach {
							it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(targetedBlock.location)) / explosionDamageRadius)
						}
						setChestFlag(chest, "locked", ByteTag.valueOf(false))
						event.player.userError("You failed to defuse the bomb!")
						failed = true
						cancel()
					}

					if (!failed) {
						// button logic
						buttonList.forEach { button ->
							if (button.type != STONE_BUTTON) {
								iteration = (timeLimit - 1) * 20
								chest.location.world.playSound(chest.location, ENTITY_WARDEN_SONIC_CHARGE, 10.0f, 1.0f)
								event.player.userError("You tampered with the bomb's disarming mechanism!")
								failed = true
								return@forEach
							}
							val buttonData = button.blockData as Switch
							if (buttonData.isPowered) {
								buttonData.isPowered = false
								selected.add(button.getRelative(DOWN).type)
								button.location.world.playSound(button.location, ITEM_FLINTANDSTEEL_USE, 5.0f, 1.0f)
								event.player.hint("The bomb mechanism clicks...")
							}
							button.blockData = buttonData
						}

						// failure
						if (selected.isNotEmpty() && (selected.size > correctOrder.size || selected.last() != correctOrder[selected.size - 1])
						) {
							iteration = (timeLimit - 1) * 20
							chest.location.world.playSound(chest.location, ENTITY_WARDEN_SONIC_CHARGE, 10.0f, 1.0f)
							event.player.userError("You pressed the button in the wrong order!")
							failed = true
						}

						// success
						if (selected == correctOrder) {
							setChestFlag(chest, "locked", ByteTag.valueOf(false))
							setChestFlag(chest, "inactive", ByteTag.valueOf(true))
							event.player.success("You successfully defused the bomb! The chest is now unlocked.")
							chest.location.world.playSound(chest.location, BLOCK_FIRE_EXTINGUISH, 5.0f, 0.0f)
							cancel()
						}
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
	val DEFENSE_BOTS = register(
		object : Encounter(identifier = "DEFENSE_BOTS") {
			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!
				val chest = (targetedBlock.state as? Chest) ?: return

				val keyCode = targetedBlock.location.toBlockPos().hashCode()
					.toString().toList().chunked(4).map { chars ->
						chars.joinToString().filter { it.isDigit() }.toInt().toChar()
				}.joinToString()

				if ((event.item?.lore()?.get(0) as? TextComponent)?.content() == keyCode) {
					event.player.success("The key card unlocked the chest!")

					setChestFlag(chest, "locked", ByteTag.valueOf(false))
					setChestFlag(chest, "inactive", ByteTag.valueOf(true))
					return
				}

				if (getChestFlag(chest, "locked") as? ByteTag == ByteTag.valueOf(true)) {
					event.isCancelled = true
					event.player.alert("The chest was still locked! More security droids have appeared!")
				} else {
					// Not a success condition, just if it hasn't been set yet
					setChestFlag(chest, "locked", ByteTag.valueOf(true))

					event.player.alert("The disturbance you caused has activated ancient security droids!")
					event.player.hint("Maybe one of them still has a card to open this chest...")
				}

				val blocks = getBlocks(chest.world, chest.location.toBlockPos(), 10.0) { checkAir(it) && it.isSolid}
				val firstFour = blocks.shuffled().subList(0, 3)

				for (block in firstFour) {
					val blockAboveLoc = block.location.add(Location(chest.world, 0.0, 1.0, 0.0)).toCenterLocation()

					chest.world.castSpawnEntity<Skeleton>(blockAboveLoc, SKELETON).apply {
						this.equipment.itemInMainHandDropChance = 0.0f
						this.equipment.itemInOffHandDropChance = 1.0f

						val weirdPistol = PISTOL.constructItemStack()
						weirdPistol.type = Material.BOW
						weirdPistol.updateMeta {
							it.displayName(
								text("Rusty Blaster Pistol").color(TextColor.fromHexString("#802716"))
									.decoration(TextDecoration.ITALIC, false)
									.decoration(TextDecoration.BOLD, false)
							)
						}

						val keyCard = ItemStack(Material.PAPER).updateMeta {
							it.displayName(
								text("Key Card").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE)
							)
							it.lore(
								listOf(text(keyCode).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA))
							)
						}

						this.equipment.setItemInOffHand(keyCard)
						this.equipment.setItemInMainHand(weirdPistol)
						this.customName(text("Security Droid").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
						this.isCustomNameVisible = true
						this.isSilent = true
						this.isPersistent = false
					}
				}
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "horizonsend:chests/guns")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)

	@Suppress("Unused")
	val DEFENSE_MATRIX = register(
		object : Encounter(identifier = "DEFENSE_MATRIX") {
			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return

				setChestFlag(chest, "locked", ByteTag.valueOf(true))
				event.player.alert("[INTRUSION DETECTED] ... [ACTIVATING DEFENSE MATRIX]")

				var iteration = 0

				val playerLocation = event.player.eyeLocation.toCenterLocation().toVector()

				val blocks = getBlocks(
					event.player.world,
					chest.location.toBlockPos(),
					30.0,
				) {
					val rayCast = if (!it.isEmpty) {
						val blockLocation = it.location.toCenterLocation()
						val vector = blockLocation.toVector().subtract(playerLocation)

						it.world.rayTrace(
							blockLocation,
							vector,
							distance(
								blockLocation.x,
								blockLocation.y,
								blockLocation.z,
								playerLocation.x,
								playerLocation.y,
								playerLocation.z
							),
							FluidCollisionMode.NEVER,
							true,
							0.5,
							null
						)?.hitBlock
					} else it

					!it.isEmpty && rayCast == null
				}.shuffled().subList(0, 5)

				val defenseNodes = mutableListOf<ShulkerBullet>()

				for (block in blocks) {
					defenseNodes.add(
							event.player.world.castSpawnEntity<ShulkerBullet>(
								block.location.toCenterLocation(),
								EntityType.SHULKER_BULLET
							).apply {
								this.target = event.player

								this.flightSteps = 0
								this.targetDelta = Vector(0.0, 0.0, 0.0)
							}
					)
				}

				runnable {
					iteration++

					// 3 minute timeout
					if (iteration > 60) {
						cancel()
					}

					defenseNodes.removeAll { it.isDead }

					for (defenseNode in defenseNodes) {

						GuardianLaser(
							defenseNode.location,
							event.player,
							50,
							30
						).durationInTicks().apply { start(IonServer) }
					}

				}.runTaskTimer(IonServer, 20L, 60L)
			}

			override fun constructChestState(): Pair<BlockState, CompoundTag?> {
				val tileEntityData = CompoundTag()

				tileEntityData.putString("id", "minecraft:chest")
				tileEntityData.putString("LootTable", "horizonsend:chests/guns")
				return Blocks.CHEST.defaultBlockState() to tileEntityData
			}
		}
	)

	@Suppress("Unused")
	val PASSCODE = register(
		object : Encounter(identifier = "PASSCODE") {

			val headID = mapOf(
				0 to UUID.fromString("cb34f183-2b0a-44a4-b87c-df957a63a4f0"),
				1 to UUID.fromString("606d2a18-b91c-4f7f-9b0d-b3cd322e9214"),
				2 to UUID.fromString("e4d886d5-331e-4558-8f69-4cca32085bc9"),
				3 to UUID.fromString("eb6e7446-99b7-4b82-ae73-a93ab08ec12c"),
				4 to UUID.fromString("e4e8f360-b896-45fd-ab07-6983d871814a"),
				5 to UUID.fromString("529ee22e-2500-41e0-86ee-9855ec6fd9f1"),
				6 to UUID.fromString("3a54360c-a127-42c6-9b28-fda710ae64d3"),
				7 to UUID.fromString("eabe07f6-08ac-4aa3-a151-370907fcbead"),
				8 to UUID.fromString("5439b6ba-efc0-4baa-b72c-94de0bb9d79a"),
				9 to UUID.fromString("a67e73f4-c989-4586-b571-561fc2dc069e")
			)

			val skinID = mapOf(
				0 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZhNDU5MTFiMTYyOThjZmNhNGIyMjkxZWVkYTY2NjExM2JjNmYyYTM3ZGNiMmVjZDhjMjc1NGQyNGVmNiJ9fX0=",
				1 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FmMWIyODBjYWI1OWY0NDY5ZGFiOWYxYTJhZjc5MjdlZDk2YTgxZGYxZTI0ZDUwYThlMzk4NGFiZmU0MDQ0In19fQ==",
				2 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRiMWUxZDQyNjEyM2NlNDBjZDZhNTRiMGY4NzZhZDMwYzA4NTM5Y2Y1YTZlYTYzZTg0N2RjNTA3OTUwZmYifX19",
				3 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTA0Y2NmOGI1MzMyYzE5NmM5ZWEwMmIyMmIzOWI5OWZhY2QxY2M4MmJmZTNmN2Q3YWVlZGMzYzMzMjkwMzkifX19",
				4 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI0ZmMxOGU5NzVmNGYyMjJkODg1MjE2ZTM2M2FkYzllNmQ0NTZhYTI5MDgwZTQ4ZWI0NzE0NGRkYTQzNmY3In19fQ==",
				5 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ4YjIyMjM5NzEyZTBhZDU3OWE2MmFlNGMxMTUxMDNlNzcyODgyNWUxNzUwOGFjZDZjYzg5MTc0ZWU4MzgifX19",
				6 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWVlZmJhZDE2NzEyYTA1Zjk4ZTRmMGRlNWI0NDg2YWYzOTg3YjQ2ZWE2YWI0ZTNiZTkzZDE0YTgzMmM1NmUifX19",
				7 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNlNjlmYTk0MmRmM2Q1ZWE1M2EzYTk3NDkxNjE3NTEwOTI0YzZiOGQ3YzQzNzExOTczNzhhMWNmMmRlZjI3In19fQ==",
				8 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2QxODRmZDRhYjUxZDQ2MjJmNDliNTRjZTdhMTM5NWMyOWYwMmFkMzVjZTVhYmQ1ZDNjMjU2MzhmM2E4MiJ9fX0=",
				9 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIyNDU0YTVmYWEyNWY3YzRmNTc3MWQ1MmJiNGY1NWRlYjE5MzlmNzVlZmQ4ZTBhYzQyMTgxMmJhM2RjNyJ9fX0="
			)

			val coordinates = mapOf(
				0 to Pair(1, 3),
				1 to Pair(0, 2),
				2 to Pair(1, 2),
				3 to Pair(2, 2),
				4 to Pair(0, 1),
				5 to Pair(1, 1),
				6 to Pair(2, 1),
				7 to Pair(0, 0),
				8 to Pair(1, 0),
				9 to Pair(2, 0)
			)

			fun paneInteractCheck(player: Player, chest: Chest, passcode: String, currentCode: String) {
				when (compareCode(passcode, currentCode)) {
					false -> playerFails(player)
					true -> playerSucceeds(player, chest)
					null -> playerContinues(chest)
				}
			}

			fun compareCode(passcode: String, currentCode: String): Boolean? {
				return if (currentCode.isNotEmpty() && (currentCode.length > passcode.length || currentCode.last() != passcode[currentCode.length - 1])) {
					false
				} else if (currentCode == passcode) {
					true
				} else null
			}

			fun playerFails(player: Player) {
				player.closeInventory()
				player.userError("You entered the wrong passcode!")
				player.playSound(player, BLOCK_NOTE_BLOCK_BASS, 5.0f, 1.0f)
			}

			fun playerSucceeds(player: Player, chest: Chest) {
				player.closeInventory()
				player.success("You entered the correct passcode! The chest is now unlocked.")
				chest.location.world.playSound(chest.location, BLOCK_NOTE_BLOCK_HARP, 5.0f, 2.0f)
				setChestFlag(chest, "inactive", ByteTag.valueOf(true))
			}

			fun playerContinues(chest: Chest) {
				chest.location.world.playSound(chest.location, BLOCK_NOTE_BLOCK_SNARE, 5.0f, 1.0f)
			}

			var passcode = ""

			override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {
				// Generate a new passcode unique to each wreck
				passcode = randomInt(0, 999999).toString().padStart(6, '0')
			}

			override fun onChestInteract(event: PlayerInteractEvent) {
				val targetedBlock = event.clickedBlock!!

				event.isCancelled = true
				val chest = (targetedBlock.state as? Chest) ?: return
				var currentCode = ""

				event.player.information("DEBUG: $passcode")

				MenuHelper.apply {
					val pane = staticPane(3, 0, 3, 4)

					for (i in 0..9) {
						pane.addItem(
							guiButton(skullItem(i.toString(), headID[i]!!, skinID[i]!!)) {
								currentCode += i.toString()
								event.player.information("DEBUG: $currentCode")
								paneInteractCheck(event.player, chest, passcode, currentCode)
							}.setName(MiniMessage.miniMessage().deserialize(i.toString())),
							coordinates[i]!!.first, coordinates[i]!!.second
						)
					}

					gui(4, "test").withPane(pane).show(event.player)
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
	operator fun get(serialzedEncounter: CompoundTag): Encounter? {
		val identifier = serialzedEncounter.getString("Encounter Identifier")

		return encounters[identifier]
	}

	operator fun get(chest: Chest): Encounter? {
		val wreckData = readChunkCompoundTag(chest.chunk, NamespacedKeys.WRECK_ENCOUNTER_DATA) ?: return null

		val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)
		for (wreck in existingWrecks) {
			wreck as CompoundTag

			if (!encounterMatchesChest(wreck, chest)) continue
			if (wreck.getBoolean("inactive")) continue

			return get(wreck.getString("Encounter Identifier"))
		}

		return null
	}

	fun setChunkEncounters(chunk: Chunk, newData: CompoundTag) {
		val byteArray = ByteArrayOutputStream()

		val dataOutput = DataOutputStream(byteArray)
		NbtIo.write(newData, dataOutput)

		// Update PDCs
		chunk.persistentDataContainer.set(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY,
			byteArray.toByteArray()
		)
	}

	fun getChunkEncounters(chunk: Chunk): CompoundTag? {
		val pdc = chunk.persistentDataContainer.get(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY
		) ?: return null

		val bos = ByteArrayInputStream(
			pdc,
			0,
			pdc.size
		)

		return NbtIo.read(DataInputStream(bos))
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
		val wreckData = readChunkCompoundTag(chest.chunk, NamespacedKeys.WRECK_ENCOUNTER_DATA) ?: return

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

	fun getBlocks(world: World, origin: BlockPos, radius: Double, filter: (Block) -> Boolean): List<Block> {
		val radiusSquared = radius * radius

		val blocks = mutableListOf<Block>()

		for (x in (origin.x - ceil(radius).toInt())..(origin.x + ceil(radius).toInt())) {
			val xSquared = (x - origin.x) * (x - origin.x)

			for (y in (origin.y - ceil(radius).toInt())..(origin.y + ceil(radius).toInt())) {
				val ySquared = (y - origin.y) * (y - origin.y)
				for (z in (origin.z - ceil(radius).toInt())..(origin.z + ceil(radius).toInt())) {
					val zSquared = (z - origin.z) * (z - origin.z)

					if (xSquared + ySquared + zSquared > radiusSquared) continue

					val block = world.getBlockAt(x, y, z)

					if (filter(block)) blocks += block
				}
			}
		}

		return blocks
	}

	fun checkAir(block: Block): Boolean {
		val up1 = block.getRelative(UP)
		val up2 = up1.getRelative(UP)

		return up1.isEmpty && up2.isEmpty
	}

	fun encounterMatchesChest(wreck: CompoundTag, chest: Chest): Boolean = (
			wreck.getInt("x") == chest.x &&
					wreck.getInt("y") == chest.y &&
					wreck.getInt("z") == chest.z)

	fun createLootChest(lootTable: String): CompoundTag {
		val tileEntityData = CompoundTag()
		tileEntityData.putString("id", "minecraft:chest")
		tileEntityData.putString("LootTable", lootTable)
		return tileEntityData
	}
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
		return CHEST.createBlockData().nms to null
	}

	open fun onChestInteract(event: PlayerInteractEvent) {}

	open fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {}
}

enum class SecondaryChests(val blockState: BlockState, val NBT: CompoundTag?, val money: Int?) {
	REPAIR_MATERIALS(
		Blocks.CHEST.defaultBlockState(),
		createLootChest("horizonsend:chests/starship_resource"),
		500
	),
	FOOD(Blocks.CHEST.defaultBlockState(), null, 500),
	GUN_PARTS(Blocks.CHEST.defaultBlockState(), null, 500),
	POWER_ARMOR_MODS(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_LOW(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_MEDIUM(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_HIGH(Blocks.CHEST.defaultBlockState(), null, 500);

	companion object {


		private val map = SecondaryChests.values().associateBy { it.name }

		operator fun get(value: String): SecondaryChests? = map[value]
	}
}
