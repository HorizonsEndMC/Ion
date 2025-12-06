package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist.MediumReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class MediumReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: MediumReactorMultiblock) :
	SupercapitalReactorSubsystem<MediumReactorMultiblock>(starship, sign, multiblock)
