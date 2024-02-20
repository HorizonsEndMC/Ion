package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.supercapreactor.SupercapReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class SupercapReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: SupercapReactorMultiblock) :
		AbstractMultiblockSubsystem<SupercapReactorMultiblock>(starship, sign, multiblock)
