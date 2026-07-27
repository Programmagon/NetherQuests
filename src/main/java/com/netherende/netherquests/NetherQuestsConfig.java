package com.netherende.netherquests;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NetherQuestsConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> BAR_COLOR;

    static {
        BUILDER.push("ui");

        BAR_COLOR = BUILDER
                .comment("Hex-Farbcode fuer die Trennleiste in ARGB/RGB (z.B. FFAA0000 fuer Rot oder 00FF00 fuer Gruen)")
                .translation("netherquests.config.bar_color")
                .define("barColor", "FFAA0000");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static int getBarColor() {
        try {
            String hex = BAR_COLOR.get().replace("#", "").trim();
            if (hex.length() == 6) {
                hex = "FF" + hex;
            }
            return (int) Long.parseLong(hex, 16);
        } catch (Exception e) {
            return 0xFFAA0000;
        }
    }
}