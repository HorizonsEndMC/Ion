package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist.FauxReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class FauxReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: FauxReactorMultiblock) :
	SupercapitalReactorSubsystem<FauxReactorMultiblock>(starship, sign, multiblock)
