package com.example.examplemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class VerifyConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> WELCOME_MESSAGE;
    public static final ModConfigSpec.IntValue MAX_MANA;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        WELCOME_MESSAGE = builder.comment("A welcome message shown in chat")
                .translation("examplemod.config.welcome_message")
                .define("welcome_message", "Hello!");
        MAX_MANA = builder.comment("Max mana points").defineInRange("max_mana", 100, 1, 10000);
        builder.pop();

        SPEC = builder.build();
    }
}
