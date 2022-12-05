package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringSingleShotBlaster
import net.horizonsend.ion.server.customitems.blasters.constructors.Magazine
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("Unused")
object Sniper : AmmoRequiringSingleShotBlaster() {
	override val requiredAmmo: Magazine = StandardMagazine
	override val customItemlist: CustomItemList = CustomItemList.SNIPER

	override val singleShotWeaponBalancing: SingleShotWeaponBalancing
		get() = IonServer.Ion.balancing.energyWeapons.sniper

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {
		super.onSecondaryInteract(source, item)
	}

	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {
		val player = entity as? Player
		val craftPlayer = player as? CraftPlayer
	}
}