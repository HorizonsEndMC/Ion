package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist.SmallReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class SmallReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: SmallReactorMultiblock) :
	SupercapitalReactorSubsystem<SmallReactorMultiblock>(starship, sign, multiblock)
