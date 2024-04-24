package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.supercapreactor.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class CruiserReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CruiserReactorMultiblock) :
		AbstractMultiblockSubsystem<CruiserReactorMultiblock>(starship, sign, multiblock)
