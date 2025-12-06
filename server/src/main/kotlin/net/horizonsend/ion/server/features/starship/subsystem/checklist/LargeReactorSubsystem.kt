package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist.LargeReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class LargeReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: LargeReactorMultiblock) :
	SupercapitalReactorSubsystem<LargeReactorMultiblock>(starship, sign, multiblock)
