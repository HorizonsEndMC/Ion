package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.core.registration.keys.FrontierNationBuffTypeKeys
import net.horizonsend.ion.server.features.cache.PlayerCache
import org.bukkit.entity.Player

object FrontierNationBuffTypes {
    val SHIELD_RESISTANCE = FrontierNationBuffType(FrontierNationBuffTypeKeys.SHIELD_RESISTANCE, 0.05)
    val TURRET_DAMAGE = FrontierNationBuffType(FrontierNationBuffTypeKeys.TURRET_DAMAGE, 0.03)
    val SHIELD_REGENERATION = FrontierNationBuffType(FrontierNationBuffTypeKeys.SHIELD_REGENERATION, 0.03)
    val CRUISE_SPEED = FrontierNationBuffType(FrontierNationBuffTypeKeys.CRUISE_SPEED, 1.0)
    val DIRECT_CONTROL_SPEED = FrontierNationBuffType(FrontierNationBuffTypeKeys.DIRECT_CONTROL_SPEED, 1.0)
    val ACCELERATION = FrontierNationBuffType(FrontierNationBuffTypeKeys.ACCELERATION, 0.5)
    val JUMP_WARMUP = FrontierNationBuffType(FrontierNationBuffTypeKeys.JUMP_WARMUP, 2.0)
    val CONTACT_RANGE = FrontierNationBuffType(FrontierNationBuffTypeKeys.CONTACT_RANGE, 500.0)

    fun isEffectActive(player: Player, buffType: FrontierNationBuffType): Boolean {
        val frontierNationId = PlayerCache[player].frontierNationOid ?: return false
        val activatedBuffs = FrontierNationCache[frontierNationId].activatedBuffs
        return activatedBuffs.contains(buffType.key.key)
    }
}

data class FrontierNationBuffType(
    override val key: IonRegistryKey<FrontierNationBuffType, out FrontierNationBuffType>,
    val value: Double
) : Keyed<FrontierNationBuffType>
