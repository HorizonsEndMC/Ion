package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.BattleCruiserReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class BattlecruiserReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: BattleCruiserReactorMultiblock) :
	SupercapitalReactorSubsystem<BattleCruiserReactorMultiblock>(starship, sign, multiblock)
