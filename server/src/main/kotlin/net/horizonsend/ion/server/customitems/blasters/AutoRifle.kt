package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringSingleShotBlaster
import net.horizonsend.ion.server.customitems.blasters.constructors.Magazine
import net.starlegacy.util.Tasks.syncDelay
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack


@Suppress("Unused")
object AutoRifle : AmmoRequiringSingleShotBlaster() {
	override val requiredAmmo: Magazine = StandardMagazine
	override val customItemlist: CustomItemList = CustomItemList.AUTO_RIFLE

	override val singleShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
		get() = IonServer.Ion.balancing.energyWeapons.autoRifle

	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) { // Allows fire above 300 rpm
		val repeatCount = if (singleShotWeaponBalancing.timeBetweenShots >= 4) { 1 } else { 4 / singleShotWeaponBalancing.timeBetweenShots }

		for (count in 0..repeatCount) {
			syncDelay(count.toLong()) { super.onSecondaryInteract(entity, item) }
		}
	}
}