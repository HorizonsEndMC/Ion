package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.nations.FrontierNationBuffType

object FrontierNationBuffTypeKeys : KeyRegistry<FrontierNationBuffType>(RegistryKeys.FRONTIER_NATION_BUFF_TYPE, FrontierNationBuffType::class) {
    val SHIELD_RESISTANCE = registerTypedKey<FrontierNationBuffType>("SHIELD_RESISTANCE")
    val TURRET_DAMAGE = registerTypedKey<FrontierNationBuffType>("TURRET_DAMAGE")
    val SHIELD_REGENERATION = registerTypedKey<FrontierNationBuffType>("SHIELD_REGENERATION")
    val CRUISE_SPEED = registerTypedKey<FrontierNationBuffType>("CRUISE_SPEED")
    val DIRECT_CONTROL_SPEED = registerTypedKey<FrontierNationBuffType>("DIRECT_CONTROL_SPEED")
    val ACCELERATION = registerTypedKey<FrontierNationBuffType>("ACCELERATION")
    val JUMP_WARMUP = registerTypedKey<FrontierNationBuffType>("JUMP_WARMUP")
    val CONTACT_RANGE = registerTypedKey<FrontierNationBuffType>("CONTACT_RANGE")
}