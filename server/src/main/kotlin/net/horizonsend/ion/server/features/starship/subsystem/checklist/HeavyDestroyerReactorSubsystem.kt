package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.checklist.HeavyDestroyerReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class HeavyDestroyerReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: HeavyDestroyerReactorMultiblock) :
		AbstractMultiblockSubsystem<HeavyDestroyerReactorMultiblock>(starship, sign, multiblock)
