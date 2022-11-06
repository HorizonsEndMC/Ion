package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.inventory.ItemStack

@Suppress("Unused")
object Rifle : AmmoRequiringSingleShotBlaster() {
	override val requiredAmmo: ItemStack = CustomItems.BATTERY_MEDIUM.singleItem()
	override val customItemlist: CustomItemList = CustomItemList.RIFLE

	override val singleShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
		get() = IonServer.Ion.balancing.energyWeapons.rifle
}