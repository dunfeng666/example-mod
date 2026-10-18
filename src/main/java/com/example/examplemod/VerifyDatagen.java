package com.example.examplemod;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootConsumer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class VerifyDatagen {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new RecipeProvider(output, lookupProvider) {
            @Override
            protected void buildRecipes(RecipeOutput recipeOutput) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, VerifyItems.RUBY.get())
                        .pattern("RR")
                        .pattern("RR")
                        .define('R', VerifyItems.RUBY.get())
                        .unlockedBy("has_ruby", has(VerifyItems.RUBY.get()))
                        .save(recipeOutput);
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, VerifyItems.RUBY.get(), 4)
                        .requires(VerifyBlocks.RUBY_BLOCK.get())
                        .unlockedBy("has_ruby_block", has(VerifyBlocks.RUBY_BLOCK.get()))
                        .save(recipeOutput);
            }
        });

        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        new RubyBlockLoot(),
                        LootContextParamSets.BLOCK)),
                lookupProvider));

        generator.addProvider(event.includeClient(), new BlockStateProvider(output, ExampleMod.MODID, helper) {
            @Override
            protected void registerStatesAndModels() {
                simpleBlockWithItem(VerifyBlocks.RUBY_BLOCK.get(),
                        models().cubeAll("ruby_block", modLoc("block/ruby_block")));
            }
        });

        generator.addProvider(event.includeClient(), new ItemModelProvider(output, ExampleMod.MODID, helper) {
            @Override
            protected void registerModels() {
                basicItem(VerifyItems.RUBY.get());
            }
        });

        generator.addProvider(event.includeClient(), new LanguageProvider(output, ExampleMod.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                add(VerifyItems.RUBY.get(), "Ruby");
                add("itemGroup.examplemod", "Example Mod Tab");
            }
        });
    }

    public static class RubyBlockLoot implements LootTableSubProvider {
        @Override
        public void generate(HolderLookup.Provider provider, LootConsumer consumer) {
            consumer.accept(VerifyBlocks.RUBY_BLOCK.get().getLootTable(),
                    LootTable.lootTable().withPool(LootPool.lootPool()
                            .set(ConstantValue.exactly(1.0f))
                            .add(LootItem.lootTableItem(VerifyBlocks.RUBY_BLOCK.get()))));
        }
    }
}
