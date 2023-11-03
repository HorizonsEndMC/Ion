package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

/**
 * Marks this subsystem as having a ship-wide cooldown.
 * After a battery of shots is fired, the cooldown is applied to all of this subsystem.
 * this is needed now for light weapons or you will have no cooldown, only your power draw
 **/
interface StarshipCooldownSubsystem
