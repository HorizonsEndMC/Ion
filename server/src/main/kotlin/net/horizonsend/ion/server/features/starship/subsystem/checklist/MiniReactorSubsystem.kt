package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist.MiniReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class MiniReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: MiniReactorMultiblock) :
	SupercapitalReactorSubsystem<MiniReactorMultiblock>(starship, sign, multiblock)
