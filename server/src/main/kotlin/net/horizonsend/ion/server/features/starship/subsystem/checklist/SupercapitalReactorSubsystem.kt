package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.AbstractReactorCore
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

abstract class SupercapitalReactorSubsystem<T : AbstractReactorCore>(
	starship: ActiveStarship,
	sign: Sign,
	multiblock: T,
) : AbstractMultiblockSubsystem<T>(starship, sign, multiblock)
