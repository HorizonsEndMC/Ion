package net.starlegacy.javautil;

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
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.enginehub.linbus.format.snbt.impl.LinSnbtWriter;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SignUtils {
    private static final GsonComponentSerializer gson = GsonComponentSerializer.gson();

    public static Component convertLine(String jsonLine) {
        if (jsonLine == null) return Component.empty();

		return gson.deserialize(jsonLine);
    }

	public static SignData readSignData(LinCompoundTag nbt) {
		Component[] lines = readLines(nbt);
		Map<String, Tag> tags;
		tags = readPDC(nbt);

		return new SignData(lines, tags);
	}

    public static Component[] readLines(LinCompoundTag nbt) {
		Component[] lines = new Component[] { Component.empty(), Component.empty(), Component.empty(), Component.empty() };

        if (nbt == null) {
            return lines;
        }

		var keys = nbt.value().keySet();

		// Handle 1.20 format signs
		if (keys.contains("front_text") && !keys.contains("Text1")) {
			LinCompoundTag textCompound = nbt.getTag("front_text", LinTagType.compoundTag());

			var messages = textCompound.getListTag("messages", LinTagType.stringTag()).value();

			return messages.stream().map( (tag)-> convertLine(tag.value()) ).toArray(Component[]::new);
		}

		// Handle legacy signs
        for (int i = 0; i < 4; i++) {
            lines[i] = convertLine(nbt.getTag("Text" + (i + 1), LinTagType.stringTag()).value());
        }

        return lines;
    }

	public static Map<String, Tag> readPDC(LinCompoundTag nbt) {
		if (nbt == null) {
			return new HashMap<>();
		}

		if (!nbt.value().containsKey("PublicBukkitValues")) {
			return new HashMap<>();
		}

		LinCompoundTag pdc = nbt.getTag("PublicBukkitValues", LinTagType.compoundTag());
		CompoundTag pdcNmsCompound;
		try {
			LinSnbtWriter writer = new LinSnbtWriter();
			StringWriter string = new StringWriter();
			writer.write(string, pdc.linStream());

			pdcNmsCompound = NbtUtils.snbtToStructure(string.getBuffer().toString());
		} catch (Throwable e) {
			IonServer.INSTANCE.getSLF4JLogger().error("Exception reading persistent data container: " + e.getMessage());
			e.printStackTrace();
			return new HashMap<>();
		}

		Set<String> keys = pdcNmsCompound.getAllKeys();

		//noinspection DataFlowIssue
		return keys.stream().collect(Collectors.toMap(v -> v, pdcNmsCompound::get));
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
