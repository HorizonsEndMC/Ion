package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.SECONDARY_CHEST_MONEY
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.minecraft.nbt.CompoundTag
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.DOUBLE
import java.util.Random

enum class SecondaryChest(val NBT: CompoundTag, val money: (Double) -> Double) {
	REPAIR_MATERIALS(Encounters.createLootChest("horizonsend:chests/starship_resource"), { input: Double -> input * 2750 + 2250 }),
	FOOD(Encounters.createLootChest("horizonsend:chests/food"), { input: Double -> input * 2750 + 2250 }),
	GUN_PARTS(Encounters.createLootChest("horizonsend:chests/gun_parts"), { input: Double -> input * 2750 + 2250 }),
	POWER_ARMOR_MODS(Encounters.createLootChest("horizonsend:chests/power_armor_mods"), { input: Double -> input * 2750 + 2250 }),
	ORES_LOW(Encounters.createLootChest("horizonsend:chests/low_tier_ores"), { input: Double -> input * 2750 + 2250 }),
	ORES_MEDIUM(Encounters.createLootChest("horizonsend:chests/mid_tier_ores"), { input: Double -> input * 2750 + 2250 }),
	ORES_HIGH(Encounters.createLootChest("horizonsend:chests/high_tier_ores"), { input: Double -> input * 2750 + 2250 });

	companion object {
		private val map = SecondaryChest.values().associateBy { it.name }
		operator fun get(value: String): SecondaryChest? = map[value]
		operator fun get(chest: Chest): SecondaryChest? = map[chest.persistentDataContainer.get(
            NamespacedKeys.SECONDARY_CHEST,
            PersistentDataType.STRING
        )]

		val random = Random()

		fun giveReward(player: Player, chest: Chest) {
			if (chest.persistentDataContainer.get(NamespacedKeys.INACTIVE, PersistentDataType.STRING) == "true") return
			chest.persistentDataContainer.set(NamespacedKeys.INACTIVE, PersistentDataType.STRING, "true")

			val calcMoney = chest.persistentDataContainer.get(SECONDARY_CHEST_MONEY, DOUBLE) ?: return

			VAULT_ECO.depositPlayer(player, calcMoney)
			player.success("You found $calcMoney credits stashed in the chest!")
			chest.update()
		}
	}
}
