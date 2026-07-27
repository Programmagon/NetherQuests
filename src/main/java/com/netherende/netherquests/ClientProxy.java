package com.netherende.netherquests;

import net.minecraft.client.Minecraft;

public class ClientProxy {
    public static void openQuestScreen() {
        Minecraft.getInstance().setScreen(new QuestScreen());
    }
}