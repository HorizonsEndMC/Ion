package net.horizonsend.ion.server.features.multiblock.type.starshipweapon

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.DamageTypeKeys
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign
import org.bukkit.damage.DamageType

// TODO: Make signless multiblocks an actual thing
abstract class SignlessStarshipWeaponMultiblock<TSubsystem : WeaponSubsystem> : Multiblock(), SubsystemMultiblock<TSubsystem> {
	abstract val key: String
	val damageTypeKey: TypedKey<DamageType> by lazy { DamageTypeKeys.create(Key.key("hoizonsend", key)) }
	val damageType: DamageType by lazy { RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).getOrThrow(damageTypeKey) }

	override val signText = arrayOf<Component?>(null, null, null, null)

	override val name: String = javaClass.simpleName

	override fun matchesSign(lines: List<Component>): Boolean {
		return false
	}

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return false
	}
}
