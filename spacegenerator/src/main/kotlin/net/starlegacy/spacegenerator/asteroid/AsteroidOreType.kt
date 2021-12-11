package net.starlegacy.spacegenerator.asteroid

import net.starlegacy.feature.misc.CustomBlock
import net.starlegacy.feature.misc.CustomBlocks
import org.bukkit.Material
import org.bukkit.block.data.BlockData

enum class AsteroidOreType(val blockData: BlockData) {
    BEDROCK(Material.BEDROCK.createBlockData()),
    IRON(Material.IRON_ORE.createBlockData()),
    COAL(Material.COAL_ORE.createBlockData()),
    GOLD(Material.GOLD_ORE.createBlockData()),
    DIAMOND(Material.DIAMOND_ORE.createBlockData()),
    REDSTONE(Material.REDSTONE_ORE.createBlockData()),
    EMERALD(Material.EMERALD_ORE.createBlockData()),
    COPPER(CustomBlocks.MINERAL_COPPER.ore.blockData),
    ALUMINUM(CustomBlocks.MINERAL_ALUMINUM.ore.blockData),
    CHETHERITE(CustomBlocks.MINERAL_CHETHERITE.ore.blockData),
    TITANIUM(CustomBlocks.MINERAL_TITANIUM.ore.blockData),
    URANIUM(CustomBlocks.MINERAL_URANIUM.ore.blockData),
    ORIOMIUM(CustomBlocks.MINERAL_ORIOMIUM.ore.blockData);
}
