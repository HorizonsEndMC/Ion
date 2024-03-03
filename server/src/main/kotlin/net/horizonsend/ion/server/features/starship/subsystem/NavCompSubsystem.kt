package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.type.navigationcomputer.NavigationComputerMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class NavCompSubsystem(starship: ActiveStarship, sign: Sign, multiblock: NavigationComputerMultiblock) :
	AbstractMultiblockSubsystem<NavigationComputerMultiblock>(starship, sign, multiblock)
