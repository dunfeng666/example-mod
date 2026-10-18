package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VerifyClientRendering {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExampleMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VerifyBlockEntity>> VERIFY_BE =
            BLOCK_ENTITIES.register("verify_be",
                    () -> BlockEntityType.Builder.of(VerifyBlockEntity::new, VerifyBlocks.RUBY_BLOCK.get()).build(null));

    public static class VerifyBlockEntity extends BlockEntity {
        public VerifyBlockEntity(BlockPos pos, BlockState state) {
            super(VERIFY_BE.get(), pos, state);
        }
    }

    public static class VerifyBlockEntityRenderer implements BlockEntityRenderer<VerifyBlockEntity> {
        @Override
        public void render(VerifyBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            // render logic goes here
        }
    }

    public static class VerifyBEWLR extends BlockEntityWithoutLevelRenderer {
        public VerifyBEWLR() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            // render logic goes here
        }
    }

    public static class VerifyClientItemExtensions implements IClientItemExtensions {
        private final VerifyBEWLR bewlr = new VerifyBEWLR();

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return bewlr;
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(VERIFY_BE.get(), context -> new VerifyBlockEntityRenderer());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new VerifyClientItemExtensions(), VerifyItems.RUBY.get());
    }
}
