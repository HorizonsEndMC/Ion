package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist.BargeReactorMultiBlock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class BargeReactorSubsystem (starship: ActiveStarship, sign: Sign, multiblock: BargeReactorMultiBlock) :
	SupercapitalReactorSubsystem<BargeReactorMultiBlock>(starship, sign, multiblock) {
	override val fuelKey: IonRegistryKey<CustomItem, GasCanister> = CustomItemKeys.GAS_CANISTER_HYDROGEN
}
