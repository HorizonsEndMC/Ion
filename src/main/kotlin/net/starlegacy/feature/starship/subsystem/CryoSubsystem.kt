package net.starlegacy.feature.starship.subsystem

import net.starlegacy.feature.misc.CryoPods
import net.starlegacy.feature.multiblock.misc.CryoPodMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.block.Sign

class CryoSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CryoPodMultiblock, var pod: CryoPods.CryoPod) :
	AbstractMultiblockSubsystem<CryoPodMultiblock>(starship, sign, multiblock)
