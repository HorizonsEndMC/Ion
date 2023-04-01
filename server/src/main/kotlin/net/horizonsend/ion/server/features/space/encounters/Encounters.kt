package net.horizonsend.ion.server.features.space.encounters

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.screens.ScreenManager.openScreen
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.nms
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object Encounters {
	private val encounters: MutableMap<String, Encounter> = mutableMapOf()

	// TODO, test encounter. Will spawn enemies when you open the chest
	val ITS_A_TRAP = register(object : Encounter(
		identifier = "ITS_A_TRAP"
	) {
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
	val TIC_TAC_TOE = register(object : Encounter(
		identifier = "TIC_TAC_TOE"
	) {
		override fun generate(chestX: Int, chestY: Int, chestZ: Int) {
			TODO("Not yet implemented")
		}

		override fun onChestInteract(event: PlayerInteractEvent) {
			//True for player, false for ai
			var whoTurn: Boolean = true
			val targetedBlock = event.clickedBlock!!
			val player = event.player
			MenuHelper.apply {MenuHelper
				this.gui(5, "Tic tac toe")
				val unclaimedButton = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
				//player marker
				val redClaimedButton = ItemStack(Material.RED_STAINED_GLASS_PANE)
				//ai marker
				val blueClaimedButton = ItemStack(Material.BLUE_STAINED_GLASS_PANE)
				val indexesOfTheButtons = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
				val items = mutableListOf<GuiItem>()
				indexesOfTheButtons.forEach { index ->
					items[index] = guiButton(unclaimedButton){
						fun checkHasWon(): Boolean? {
							//Win condition of the top row
							if ((items[12].item.type == items[13].item.type && items[13].item.type == items[14].item.type)){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition of the Middle row
							if (items[21].item.type ==  items[22].item.type  && items[22].item.type  == items[23].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition of the bottom row
							if (items[30].item.type ==  items[31].item.type  && items[31].item.type  == items[32].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition for the leftmost column
							if (items[12].item.type ==  items[21].item.type  && items[21].item.type  == items[30].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition for the middle column
							if (items[13].item.type ==  items[22].item.type  && items[22].item.type  == items[31].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition for the rightmost column
							if (items[14].item.type ==  items[23].item.type  && items[23].item.type  == items[32].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition for the right diagonal
							if (items[12].item.type ==  items[22].item.type  && items[22].item.type  == items[32].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							//Win condition for the left diagonal
							if (items[14].item.type ==  items[22].item.type  && items[22].item.type  == items[30].item.type){
								return when (items[14].item.type) {
									Material.RED_STAINED_GLASS_PANE -> true
									Material.BLUE_STAINED_GLASS_PANE -> false
									else -> null
								}
							}
							return null
						}
						if (items[index]!=blueClaimedButton && whoTurn) {
							items[index] = guiButton(redClaimedButton) {}
							if(checkHasWon() == null) {
								whoTurn = !whoTurn
							} else if(checkHasWon() == true){

							}
							//AI below (highly advanced, with 100% quantum neural deeplearning frameworks)
							if (!whoTurn){
								val freeButtons = indexesOfTheButtons.filter { items[it].item == unclaimedButton }
								if (freeButtons.isNotEmpty()) {
									items[freeButtons.random()] = guiButton(blueClaimedButton) {}
									whoTurn = !whoTurn
									checkHasWon()
								}
							} else{

							}
						}
					}
				}
				player.openPaginatedMenu("Tic Tic Toe", items)
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

	fun getByIdentifier(identifier: String): Encounter? = encounters[identifier]
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
		private val map = SecondaryChests.values().associate { it.name to it }

		operator fun get(value: String): SecondaryChests? = map[value]
	}
}
