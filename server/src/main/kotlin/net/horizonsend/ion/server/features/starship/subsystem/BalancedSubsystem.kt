package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.configuration.starship.SubsystemBalancing
import java.util.function.Supplier

interface BalancedSubsystem<T : SubsystemBalancing> {
	val balancingSupplier: Supplier<T>

	/** Balancing values for this subsystem, and projectile **/
	val balancing get() = balancingSupplier.get()
}
