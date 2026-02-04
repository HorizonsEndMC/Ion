package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.FrontierNationBuffTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.nations.FrontierNationBuffType
import net.horizonsend.ion.server.features.nations.FrontierNationBuffTypes

class FrontierNationBuffTypeRegistry : Registry<FrontierNationBuffType>(RegistryKeys.FRONTIER_NATION_BUFF_TYPE) {
    override fun getKeySet(): KeyRegistry<FrontierNationBuffType> = FrontierNationBuffTypeKeys

    override fun boostrap() {
        register(FrontierNationBuffTypeKeys.SHIELD_RESISTANCE, FrontierNationBuffTypes.SHIELD_RESISTANCE)
        register(FrontierNationBuffTypeKeys.TURRET_DAMAGE, FrontierNationBuffTypes.TURRET_DAMAGE)
        register(FrontierNationBuffTypeKeys.SHIELD_REGENERATION, FrontierNationBuffTypes.SHIELD_REGENERATION)
        register(FrontierNationBuffTypeKeys.CRUISE_SPEED, FrontierNationBuffTypes.CRUISE_SPEED)
        register(FrontierNationBuffTypeKeys.DIRECT_CONTROL_SPEED, FrontierNationBuffTypes.DIRECT_CONTROL_SPEED)
        register(FrontierNationBuffTypeKeys.ACCELERATION, FrontierNationBuffTypes.ACCELERATION)
        register(FrontierNationBuffTypeKeys.JUMP_WARMUP, FrontierNationBuffTypes.JUMP_WARMUP)
        register(FrontierNationBuffTypeKeys.CONTACT_RANGE, FrontierNationBuffTypes.CONTACT_RANGE)
    }
}