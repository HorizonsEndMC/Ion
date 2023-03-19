package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.toNMSBlockData
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent

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
		return Material.CHEST.toNMSBlockData() to null
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
