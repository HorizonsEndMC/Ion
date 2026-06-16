package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import org.bukkit.Material
import org.bukkit.block.data.BlockData

object CreditPrintBlackList {
	var creditPrintBlackList = setOf(
		Material.IRON_BLOCK,
		Material.DIAMOND_BLOCK,
		Material.OXIDIZED_COPPER,
		Material.COPPER_BLOCK,
		Material.EXPOSED_COPPER,
		Material.WEATHERED_COPPER,
		Material.WAXED_COPPER_BLOCK,
		Material.WAXED_EXPOSED_COPPER,
		Material.WAXED_WEATHERED_COPPER,
		Material.WAXED_OXIDIZED_COPPER,
		Material.REDSTONE_BLOCK,
		Material.END_PORTAL_FRAME,
		Material.REDSTONE,
		Material.DIAMOND_ORE,
		Material.GOLD_ORE,
		Material.COPPER_ORE,
		Material.IRON_ORE,
		Material.EMERALD_ORE,
		Material.DEEPSLATE_COAL_ORE,
		Material.COAL_ORE,
		Material.DEEPSLATE_COPPER_ORE,
		Material.DEEPSLATE_DIAMOND_ORE,
		Material.DEEPSLATE_EMERALD_ORE,
		Material.DEEPSLATE_GOLD_ORE,
		Material.DEEPSLATE_REDSTONE_ORE,
		Material.REDSTONE_ORE,
		Material.DEEPSLATE_IRON_ORE,
		Material.LAPIS_ORE,
		Material.NETHER_GOLD_ORE,
		Material.BEDROCK,
		Material.BARRIER,
		Material.REINFORCED_DEEPSLATE
	)

	fun checkForCreditPrintBlacklist(data: BlockData): Boolean {
		val customBlockKey = data.customBlock?.key
		val material = data.material
		if (creditPrintBlackList.contains(material)) return true

		when (customBlockKey) {
			CustomBlockKeys.TITANIUM_BLOCK -> return true
			CustomBlockKeys.URANIUM_BLOCK -> return true
			CustomBlockKeys.CHETHERITE_BLOCK -> return true
			CustomBlockKeys.ALUMINUM_BLOCK -> return true
			CustomBlockKeys.RAW_ALUMINUM_BLOCK -> return true
			CustomBlockKeys.RAW_TITANIUM_BLOCK -> return true
			CustomBlockKeys.RAW_URANIUM_BLOCK -> return true
			CustomBlockKeys.TITANIUM_ORE -> return true
			CustomBlockKeys.URANIUM_ORE -> return true
			CustomBlockKeys.ALUMINUM_ORE -> return true
			CustomBlockKeys.CHETHERITE_ORE -> return true
			CustomBlockKeys.ATAVUM_BLOCK -> return true
			CustomBlockKeys.ATAVUM_ORE -> return true
			CustomBlockKeys.ZIRCON_BLOCK -> return true
			CustomBlockKeys.ZIRCON_ORE -> return true
			CustomBlockKeys.SCORDITE_BLOCK -> return true
			CustomBlockKeys.SCORDITE_ORE -> return true
			CustomBlockKeys.VANADIUM_BLOCK -> return true
			CustomBlockKeys.VANADIUM_ORE -> return true
			CustomBlockKeys.ASSEMBLY_CORE -> return true
			CustomBlockKeys.BATTLECRUISER_REACTOR_CORE -> return true
			CustomBlockKeys.CRUISER_REACTOR_CORE -> return true
			CustomBlockKeys.MINI_REACTOR_CORE -> return true
			CustomBlockKeys.SMALL_REACTOR_CORE -> return true
			CustomBlockKeys.MEDIUM_REACTOR_CORE -> return true
			CustomBlockKeys.LARGE_REACTOR_CORE -> return true
			CustomBlockKeys.STEEL_BLOCK -> return true
			CustomBlockKeys.NETHERITE_CASING -> return true
			CustomBlockKeys.ENRICHED_URANIUM_BLOCK -> return true
		}
		return false
	}
}
