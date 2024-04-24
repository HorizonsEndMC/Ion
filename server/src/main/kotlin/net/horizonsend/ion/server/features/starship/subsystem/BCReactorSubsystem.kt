package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.supercapreactor.BCReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class BCReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: BCReactorMultiblock) :
		AbstractMultiblockSubsystem<BCReactorMultiblock>(starship, sign, multiblock)
