package com.example.examplemod;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(ExampleMod.MODID)
public class VerifyGameTests {

    @GameTest
    public static void succeedAfterDelay(GameTestHelper helper) {
        helper.runAfterDelay(1, helper::succeed);
    }

    @GameTest
    public static void rubyItemHasExpectedId(GameTestHelper helper) {
        helper.succeedWhen(() -> {
            String id = VerifyItems.RUBY.get().getDescriptionId();
            if (!id.contains("ruby")) {
                throw new GameTestAssertException("unexpected id: " + id);
            }
        });
    }
}
