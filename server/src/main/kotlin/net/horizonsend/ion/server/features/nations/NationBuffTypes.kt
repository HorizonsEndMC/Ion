package net.horizonsend.ion.server.features.nations

/*
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.core.registration.keys.NationBuffTypeKeys
import net.horizonsend.ion.server.features.cache.PlayerCache
import org.bukkit.entity.Player

object NationBuffTypes {
    val SHIELD_RESISTANCE = NationBuffType(NationBuffTypeKeys.SHIELD_RESISTANCE, 0.05)
    val TURRET_DAMAGE = NationBuffType(NationBuffTypeKeys.TURRET_DAMAGE, 0.03)
    val SHIELD_REGENERATION = NationBuffType(NationBuffTypeKeys.SHIELD_REGENERATION, 0.03)
    val CRUISE_SPEED = NationBuffType(NationBuffTypeKeys.CRUISE_SPEED, 1.0)
    val DIRECT_CONTROL_SPEED = NationBuffType(NationBuffTypeKeys.DIRECT_CONTROL_SPEED, 1.0)
    val ACCELERATION = NationBuffType(NationBuffTypeKeys.ACCELERATION, 0.5)
    val JUMP_WARMUP = NationBuffType(NationBuffTypeKeys.JUMP_WARMUP, 2.0)
    val CONTACT_RANGE = NationBuffType(NationBuffTypeKeys.CONTACT_RANGE, 500.0)

    fun isEffectActive(player: Player, buffType: NationBuffType): Boolean {
        val nationId = PlayerCache[player].nationOid ?: return false
        //val activatedBuffs = NationCache[nationId].activatedBuffs
        return activatedBuffs.contains(buffType.key.key)
    }
}

data class NationBuffType(
	override val key: IonRegistryKey<NationBuffType, out NationBuffType>,
	val value: Double
) : Keyed<NationBuffType>
 */
