package net.starlegacy.javautil;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.horizonsend.ion.server.IonServer;
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.enginehub.linbus.format.snbt.impl.LinSnbtWriter;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BannerUtils {
	public static BannerData readBannerData(LinCompoundTag nbt) {
		if (nbt == null) {
			return new BannerData(List.of(), new HashMap<>());
		}

		CompoundTag root = toNmsCompound(nbt);
		//no patterns
		if (root == null) {
			return new BannerData(List.of(), SignUtils.readPDC(nbt));
		}

		return new BannerData(readPatterns(root), SignUtils.readPDC(nbt));
	}
    /** Convert `LinCompoundTag` from WorldEdit to nms `CompoundTag`*/
	private static CompoundTag toNmsCompound(LinCompoundTag nbt) {
		try {
			LinSnbtWriter writer = new LinSnbtWriter();
			StringWriter string = new StringWriter();
			writer.write(string, nbt.linStream());

			return NbtUtils.snbtToStructure(string.getBuffer().toString());
		} catch (Throwable e) {
			IonServer.INSTANCE.getSLF4JLogger().error("Exception reading banner data: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/** get pattern layers from NBT while supporting legacy versions */
	private static List<Pattern> readPatterns(CompoundTag root) {
		//modern lowercase NBT
		Tag listTag = root.get("patterns");
		if (!(listTag instanceof ListTag)) {
			// older schematic/NBT
			listTag = root.get("Patterns");
		}

		//failed parse or no patterns
		if (!(listTag instanceof ListTag patternsTag)) {
			return List.of();
		}

		List<Pattern> patterns = new ArrayList<>();
		for (Tag tag : patternsTag) {
			if (!(tag instanceof CompoundTag patternTag)) continue;

			String rawPatternType = readString(patternTag, "pattern", "Pattern");
			PatternType patternType = getPatternType(rawPatternType);
			if (patternType == null) continue;

			DyeColor color = readColor(patternTag);
			patterns.add(new Pattern(color, patternType));
		}

		return patterns;
	}

	private static String readString(CompoundTag tag, String modernKey, String legacyKey) {
		if (tag.contains(modernKey)) return tag.getString(modernKey).get();
		if (tag.contains(legacyKey)) return tag.getString(legacyKey).get();
		return null;
	}

	private static DyeColor readColor(CompoundTag patternTag) {
		String colorName = readString(patternTag, "color", "Color");
		if (colorName != null) {
			try {
				return DyeColor.valueOf(colorName.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				// Fall through to legacy numeric color, if present.
			}
		}

		if (patternTag.contains("Color")) {
			return dyeColorFromBannerId(patternTag.getInt("Color").get());
		}

		return DyeColor.WHITE;
	}

	private static DyeColor dyeColorFromBannerId(int id) {
		return switch (id) {
			case 0 -> DyeColor.WHITE;
			case 1 -> DyeColor.ORANGE;
			case 2 -> DyeColor.MAGENTA;
			case 3 -> DyeColor.LIGHT_BLUE;
			case 4 -> DyeColor.YELLOW;
			case 5 -> DyeColor.LIME;
			case 6 -> DyeColor.PINK;
			case 7 -> DyeColor.GRAY;
			case 8 -> DyeColor.LIGHT_GRAY;
			case 9 -> DyeColor.CYAN;
			case 10 -> DyeColor.PURPLE;
			case 11 -> DyeColor.BLUE;
			case 12 -> DyeColor.BROWN;
			case 13 -> DyeColor.GREEN;
			case 14 -> DyeColor.RED;
			case 15 -> DyeColor.BLACK;
			default -> DyeColor.WHITE;
		};
	}

	@SuppressWarnings("removal")
	private static PatternType getPatternType(String rawPatternType) {
		if (rawPatternType == null || rawPatternType.isBlank()) return null;

		String keyString = rawPatternType.toLowerCase(Locale.ROOT);
		NamespacedKey key = keyString.contains(":")
				? NamespacedKey.fromString(keyString)
				: NamespacedKey.minecraft(keyString);

		if (key != null) {
			PatternType type = RegistryAccess.registryAccess()
					.getRegistry(RegistryKey.BANNER_PATTERN)
					.get(key);
			if (type != null) return type;
		}

		// Legacy schematic/banner NBT used short ids like "bs", "tts", "flo", etc.
		String legacyIdentifier = rawPatternType.contains(":")
				? rawPatternType.substring(rawPatternType.indexOf(':') + 1)
				: rawPatternType;
		return PatternType.getByIdentifier(legacyIdentifier);
	}

	static public class BannerData {
		public BannerData(List<Pattern> patterns, Map<String, Tag> persistentDataValues) {
			this.patterns = List.copyOf(patterns);
			this.persistentDataValues = persistentDataValues;
		}

		private final List<Pattern> patterns;
		private final Map<String, Tag> persistentDataValues;

		private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

		public void applyTo(Banner banner) {
			banner.setPatterns(patterns);

			persistentDataValues.remove(NamespacedKeys.INSTANCE.getMULTIBLOCK_ENTITY_DATA().asString());

			CraftPersistentDataContainer converted = new CraftPersistentDataContainer(persistentDataValues, DATA_TYPE_REGISTRY);
			converted.copyTo(banner.getPersistentDataContainer(), true);

			banner.update(false, false);
		}
	}
}
