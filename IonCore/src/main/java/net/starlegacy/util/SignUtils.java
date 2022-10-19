package net.starlegacy.util;

import com.google.gson.Gson;
import com.sk89q.jnbt.CompoundTag;
import org.bukkit.ChatColor;

import java.util.List;

public class SignUtils {
	private static Gson gson = new Gson();

	public static String convertLine(String jsonLine) {
		if (jsonLine == null) return "";
		SignNBTLine line = gson.fromJson(jsonLine, SignNBTLine.class);
		StringBuilder text = new StringBuilder(line.getText());
		if (line.getExtra() != null) {
			for (SignNBTLine signNBTLine : line.getExtra()) text.append(signNBTLine.getText());
		}
		return text.toString();
	}

	public static String[] fromCompoundTag(CompoundTag nbt) {
		String[] lines = new String[]{"", "", "", ""};

		if (nbt == null) {
			return lines;
		}

		for (int i = 0; i < 4; i++) {
			lines[i] = convertLine(nbt.getString("Text" + (i + 1)));
		}

		return lines;
	}

	public static class SignNBTLine {
		private List<SignNBTLine> extra;
		private String color;
		private String text;

		public String getText() {
			return color == null ? text : ChatColor.valueOf(color.toUpperCase()) + text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public List<SignNBTLine> getExtra() {
			return extra;
		}

		public void setExtra(List<SignNBTLine> extra) {
			this.extra = extra;
		}

		public void setColor(String color) {
			this.color = color;
		}
	}
}
