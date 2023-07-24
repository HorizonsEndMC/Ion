package net.starlegacy

import net.horizonsend.ion.server.legacy.listeners.BowHitListener
import net.starlegacy.listener.gear.DetonatorListener
import net.starlegacy.listener.gear.DoubleJumpListener
import net.starlegacy.listener.gear.PowerArmorListener
import net.starlegacy.listener.gear.PowerToolListener
import net.starlegacy.listener.gear.SwordListener
import net.starlegacy.listener.misc.*
import net.starlegacy.listener.nations.FriendlyFireListener
import net.starlegacy.listener.nations.MovementListener

val listeners = listOf(
	JoinLeaveListener,
	MovementListener,
	FriendlyFireListener,
	ProtectionListener,
	ChatListener,

	BlockListener,
	EntityListener,
	FurnaceListener,
	InteractListener,
	InventoryListener,
	BowHitListener,

	DetonatorListener,
	DoubleJumpListener,
	PowerArmorListener,
	PowerToolListener,
	SwordListener
)
