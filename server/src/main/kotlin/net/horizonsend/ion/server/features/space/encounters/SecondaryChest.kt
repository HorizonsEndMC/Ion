package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.VAULT_ECO
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

enum class SecondaryChest(val blockState: BlockState, val NBT: CompoundTag?, val money: Int?) {
	REPAIR_MATERIALS(
		Blocks.CHEST.defaultBlockState(),
        Encounters.createLootChest("horizonsend:chests/starship_resource"),
		500
	),
	FOOD(Blocks.CHEST.defaultBlockState(), null, 500),
	GUN_PARTS(Blocks.CHEST.defaultBlockState(), null, 500),
	POWER_ARMOR_MODS(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_LOW(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_MEDIUM(Blocks.CHEST.defaultBlockState(), null, 500),
	ORES_HIGH(Blocks.CHEST.defaultBlockState(), null, 500);

	companion object {
		private val map = SecondaryChest.values().associateBy { it.name }

		operator fun get(value: String): SecondaryChest? = map[value]
		operator fun get(chest: Chest): SecondaryChest? = map[chest.persistentDataContainer.get(
            NamespacedKeys.SECONDARY_CHEST,
            PersistentDataType.STRING
        )]

		fun SecondaryChest.giveReward(player: Player, chest: Chest) {
			if (chest.persistentDataContainer.get(NamespacedKeys.INACTIVE, PersistentDataType.STRING) == "true") return

			money?.let {
				chest.persistentDataContainer.set(NamespacedKeys.INACTIVE, PersistentDataType.STRING, "true")
				VAULT_ECO.depositPlayer(player, money.toDouble())
				player.success("You found $money credits stashed in the chest!")
			}
		}
	}
}
