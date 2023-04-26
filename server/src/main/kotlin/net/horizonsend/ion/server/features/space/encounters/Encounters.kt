package net.horizonsend.ion.server.features.space.encounters

import kotlin.math.ceil
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.ENCOUNTER
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.INACTIVE
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.persistence.PersistentDataType.STRING

@Suppress("Unused")
object Encounters {
	private val encounters: MutableMap<String, Encounter> = mutableMapOf()

	val BRIDGE_OF_DEATH = register(BridgeOfDeath)
	val COOLANT_LEAK    = register(CoolantLeak)
	val COW_TIPPER      = register(CowTipper)
	val DEFENSE_BOTS    = register(DefenseBots)
	val DEFENSE_MATRIX  = register(DefenseMatrix)
	val DEFUSE_BOMB     = register(DefuseBomb)
	val INFESTED        = register(Infested)
	val ITS_A_TRAP      = register(ItsATrap)
	val PASSCODE        = register(Passcode)
	val TIMED_BOMB      = register(TimedBomb)

	private fun <T : Encounter> register(encounter: T): T {
		encounters[encounter.identifier] = encounter
		return encounter
	}

	val identifiers = encounters.keys

	operator fun get(identifier: String): Encounter? = encounters[identifier]

	operator fun get(chest: Chest): Encounter? {
		if (chest.persistentDataContainer.get(INACTIVE, STRING) == "true") return null

		chest.persistentDataContainer.get(ENCOUNTER, STRING) ?: return null

		return get(chest.persistentDataContainer.get(ENCOUNTER, STRING) ?: return null)
	}

	fun getChestFlag(chest: Chest, key: NamespacedKey) : String? =
		chest.persistentDataContainer.get(key, STRING)

	fun setChestFlag(chest: Chest, key: NamespacedKey, flag: String) =
		chest.persistentDataContainer.set(key, STRING, flag)

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
		val tileEntityData = Encounter.baseChestNBT

		tileEntityData.putString("LootTable", lootTable)
		return tileEntityData
	}
}

