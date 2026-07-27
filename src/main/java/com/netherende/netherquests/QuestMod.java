package com.netherende.netherquests;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod("netherquests")
public class QuestMod {

    public QuestMod(ModContainer modContainer, IEventBus modEventBus) {
        // Items registrieren
        ModItems.ITEMS.register(modEventBus);
        
        // Network Payloads registrieren
        modEventBus.addListener(this::registerPayloads);

        // Client-Config registrieren
        modContainer.registerConfig(ModConfig.Type.CLIENT, NetherQuestsConfig.SPEC);

        // Config-Knopf im NeoForge Mod-Menü aktivieren
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        ModCreativeModeTabs.register(modEventBus);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("netherquests");
        
        registrar.playToServer(
            CompleteQuestPayload.TYPE,
            CompleteQuestPayload.STREAM_CODEC,
            CompleteQuestPayload::handleData
        );
    }
}