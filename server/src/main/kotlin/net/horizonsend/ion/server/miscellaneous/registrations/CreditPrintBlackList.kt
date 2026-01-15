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
		Material.END_PORTAL_FRAME
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
			CustomBlockKeys.ATAVUM_BLOCK -> return true
			CustomBlockKeys.ATAVUM_ORE -> return true
			CustomBlockKeys.ZIRCON_BLOCK -> return true
			CustomBlockKeys.ZIRCON_ORE -> return true
			CustomBlockKeys.SCORDITE_BLOCK -> return true
			CustomBlockKeys.SCORDITE_ORE -> return true
			CustomBlockKeys.VANADIUM_BLOCK -> return true
			CustomBlockKeys.VANADIUM_ORE -> return true
			CustomBlockKeys.KOTH_BLOCK -> return true
			CustomBlockKeys.BATTLECRUISER_REACTOR_CORE -> return true
			CustomBlockKeys.CRUISER_REACTOR_CORE -> return true
			CustomBlockKeys.MINI_REACTOR_CORE -> return true
			CustomBlockKeys.SMALL_REACTOR_CORE -> return true
			CustomBlockKeys.MEDIUM_REACTOR_CORE -> return true
			CustomBlockKeys.LARGE_REACTOR_CORE -> return true
		}
		return false
	}
}
