package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.checklist.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class CruiserReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CruiserReactorMultiblock) :
		AbstractMultiblockSubsystem<CruiserReactorMultiblock>(starship, sign, multiblock)
