package com.netherende.netherquests;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "netherquests");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NETHER_QUESTS_TAB = CREATIVE_MODE_TABS.register("nether_quests_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.QUEST_BOOK.get()))
                    .title(Component.translatable("creativetab.netherquests.tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.QUEST_BOOK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}