package net.horizonsend.ion.server.features.starship.subsystem.command_burst

import net.horizonsend.ion.server.configuration.starship.StarshipCommandBurstBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AbstractCommandBurstMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign
import java.util.function.Supplier

abstract class AbstractCommandBurstSubsystem<T : StarshipCommandBurstBalancing>(
	starship: Starship,
	sign: Sign,
	multiblock: AbstractCommandBurstMultiblock,
	val balancingSupplier: Supplier<T>
) : AbstractMultiblockSubsystem<AbstractCommandBurstMultiblock>(starship, sign, multiblock) {

	/** Balancing values for this subsystem **/
	val balancing get() = balancingSupplier.get()
	var lastActivated: Long = System.nanoTime()

	/** Cooldown between activating abilities of this command burst **/
	open val activateCooldownNanos: Long get() = balancing.activateCooldownNanos

	fun isCooledDown(): Boolean {
		return System.nanoTime() - lastActivated >= activateCooldownNanos
	}

	fun canCreateSubsystem(): Boolean {
		if (starship.type.eventShip) return true
		if (!balancing.activateRestrictions.canActivate && !starship.type.eventShip) return false
		return starship.initialBlockCount in balancing.activateRestrictions.minBlockCount..balancing.activateRestrictions.maxBlockCount
	}

	abstract fun activate()

	fun postActivate() {
		lastActivated = System.nanoTime()
	}

	abstract fun getName(): Component
}
