package com.example.examplemod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class VerifyPayloads {

    public record MyData(String name, int age) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<MyData> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "my_data"));

        public static final StreamCodec<ByteBuf, MyData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, MyData::name,
                ByteBufCodecs.VAR_INT, MyData::age,
                MyData::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class Events {
        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(
                    MyData.TYPE,
                    MyData.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(Handlers::handleClient, Handlers::handleServer));
        }
    }

    public static class Handlers {
        public static void handleClient(final MyData data, final IPayloadContext context) {
            context.enqueueWork(() -> { /* main thread work */ });
        }

        public static void handleServer(final MyData data, final IPayloadContext context) {
            PacketDistributor.sendToAllPlayers(data);
        }
    }
}
