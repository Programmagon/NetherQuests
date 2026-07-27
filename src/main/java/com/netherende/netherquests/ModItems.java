package com.netherende.netherquests;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("netherquests");

    public static final DeferredItem<Item> QUEST_BOOK = ITEMS.register("quest_book", 
        () -> new QuestBookItem(new Item.Properties().stacksTo(1))); 
}