package net.starlegacy.javautil;

import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.StringBinaryTag;
import com.sk89q.worldedit.util.nbt.TagStringIO;
import net.horizonsend.ion.server.IonServer;
import net.horizonsend.ion.server.features.machine.PowerMachines;
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataTypeRegistry;

import java.util.HashMap;
import java.util.Map;

public class SignUtils {
    private static final GsonComponentSerializer gson = GsonComponentSerializer.gson();

    public static Component convertLine(String jsonLine) {
        if (jsonLine == null) return Component.empty();

		return gson.deserialize(jsonLine);
    }

	public static SignData readSignData(CompoundBinaryTag nbt) {
		Component[] lines = readLines(nbt);
		Map<String, Tag> tags;
		tags = readPDC(nbt);

		return new SignData(lines, tags);
	}

    public static Component[] readLines(CompoundBinaryTag nbt) {
		Component[] lines = new Component[] { Component.empty(), Component.empty(), Component.empty(), Component.empty() };

        if (nbt == null) {
            return lines;
        }

		var keys = nbt.keySet();

		// Handle 1.20 format signs
		if (keys.contains("front_text") && !keys.contains("Text1")) {
			CompoundBinaryTag textCompound = nbt.getCompound("front_text");

			var messages = textCompound.getList("messages");

			return messages.stream().map( (tag)-> convertLine(((StringBinaryTag) tag).value()) ).toArray(Component[]::new);
		}

		// Handle legacy signs
        for (int i = 0; i < 4; i++) {
            lines[i] = convertLine(nbt.getString("Text" + (i + 1)));
        }

        return lines;
    }

	public static Map<String, Tag> readPDC(CompoundBinaryTag nbt) {
		if (nbt == null) {
			return new HashMap<>();
		}

		if (!nbt.keySet().contains("PublicBukkitValues")) {
			return new HashMap<>();
		}

		CompoundBinaryTag pdc = nbt.getCompound("PublicBukkitValues");
		CompoundTag pdcNmsCompound;
		try {
			pdcNmsCompound = NbtUtils.snbtToStructure(TagStringIO.get().asString(pdc));
		} catch (Throwable e) {
			IonServer.INSTANCE.getSLF4JLogger().error("Exception reading persistent data container: " + e.getMessage());
			e.printStackTrace();
			return new HashMap<>();
		}

		return pdcNmsCompound.tags;
	}

	static public class SignData {
		public SignData(Component[] lines, Map<String, Tag> persistentDataValues) {
			this.lines = lines;
			this.persistentDataValues = persistentDataValues;
		}

		Component[] lines;
		Map<String, Tag> persistentDataValues;

		private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

		public void applyTo(Sign sign) {
			for (int i = 0; i < 4; i++) {
				sign.getSide(Side.FRONT).line(i, lines[i]);
			}

			CraftPersistentDataContainer converted = new CraftPersistentDataContainer(persistentDataValues, DATA_TYPE_REGISTRY);
			converted.copyTo(sign.getPersistentDataContainer(), true);

			if (sign.getPersistentDataContainer().has(NamespacedKeys.INSTANCE.getPOWER())) {
				if (PowerMachines.INSTANCE.getPower(sign) >= 0) {
					PowerMachines.INSTANCE.setPower(sign, 0, true);
				}
			}

			sign.update(false, false);
		}
	}
}
