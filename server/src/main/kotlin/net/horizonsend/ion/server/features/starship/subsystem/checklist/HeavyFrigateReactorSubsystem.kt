package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.checklist.HeavyFrigateReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class HeavyFrigateReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: HeavyFrigateReactorMultiblock) :
		AbstractMultiblockSubsystem<HeavyFrigateReactorMultiblock>(starship, sign, multiblock)
