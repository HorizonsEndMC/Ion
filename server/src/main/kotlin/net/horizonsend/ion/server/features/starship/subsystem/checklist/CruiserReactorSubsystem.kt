package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.CruiserReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class CruiserReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CruiserReactorMultiblock) :
	SupercapitalReactorSubsystem<CruiserReactorMultiblock>(starship, sign, multiblock)
