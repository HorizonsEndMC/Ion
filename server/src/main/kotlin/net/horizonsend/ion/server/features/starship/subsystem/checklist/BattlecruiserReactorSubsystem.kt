package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.BattleCruiserReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class BattlecruiserReactorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: BattleCruiserReactorMultiblock) :
		AbstractMultiblockSubsystem<BattleCruiserReactorMultiblock>(starship, sign, multiblock)
