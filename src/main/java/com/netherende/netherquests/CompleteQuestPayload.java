package com.netherende.netherquests;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CompleteQuestPayload(String questId) implements CustomPacketPayload {

    public static final Type<CompleteQuestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("netherquests", "complete_quest"));

    public static final StreamCodec<FriendlyByteBuf, CompleteQuestPayload> STREAM_CODEC = CustomPacketPayload.codec(
        CompleteQuestPayload::write,
        CompleteQuestPayload::new
    );

    public CompleteQuestPayload(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.questId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final CompleteQuestPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Quest quest = QuestManager.getQuestById(data.questId());
            
            if (quest != null && player instanceof ServerPlayer serverPlayer && !quest.isCompleted) {
                boolean canComplete = false;

                switch (quest.reqType) {
                    case "LEVEL" -> {
                        if (player.experienceLevel >= quest.requiredAmount) {
                            canComplete = true;
                        }
                    }
                    case "ADVANCEMENT" -> {
                        ResourceLocation advLoc = ResourceLocation.tryParse(quest.requiredItem);
                        if (advLoc != null) {
                            AdvancementHolder adv = serverPlayer.getServer().getAdvancements().get(advLoc);
                            if (adv != null && serverPlayer.getAdvancements().getOrStartProgress(adv).isDone()) {
                                canComplete = true;
                            }
                        }
                    }
                    default -> { 
                        Item reqItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(quest.requiredItem));
                        int count = 0;
                        for (ItemStack stack : player.getInventory().items) {
                            if (stack.is(reqItem)) count += stack.getCount();
                        }
                        
                        if (count >= quest.requiredAmount) {
                            canComplete = true;
                            int toRemove = quest.requiredAmount;
                            for (ItemStack stack : player.getInventory().items) {
                                if (stack.is(reqItem)) {
                                    int shrinkBy = Math.min(stack.getCount(), toRemove);
                                    stack.shrink(shrinkBy);
                                    toRemove -= shrinkBy;
                                    if (toRemove <= 0) break;
                                }
                            }
                        }
                    }
                }

                if (canComplete) {
                    switch (quest.rewardType) {
                        case "LEVEL" -> {
                            player.giveExperienceLevels(quest.rewardAmount);
                        }
                        case "ADVANCEMENT" -> {
                            ResourceLocation advLoc = ResourceLocation.tryParse(quest.rewardItem);
                            if (advLoc != null) {
                                AdvancementHolder adv = serverPlayer.getServer().getAdvancements().get(advLoc);
                                if (adv != null) {
                                    for (String criterion : serverPlayer.getAdvancements().getOrStartProgress(adv).getRemainingCriteria()) {
                                        serverPlayer.getAdvancements().award(adv, criterion);
                                    }
                                }
                            }
                        }
                        case "COMMAND" -> {
                            String cmd = quest.rewardItem.replace("%player%", player.getScoreboardName());
                            if (cmd.startsWith("/")) cmd = cmd.substring(1);
                            serverPlayer.getServer().getCommands().performPrefixedCommand(
                                serverPlayer.getServer().createCommandSourceStack().withPermission(4),
                                cmd
                            );
                        }
                        default -> { 
                            Item rewItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(quest.rewardItem));
                            ItemStack rewardStack = new ItemStack(rewItem, quest.rewardAmount);
                            if (!player.getInventory().add(rewardStack)) {
                                player.drop(rewardStack, false);
                            }
                        }
                    }

                    quest.isCompleted = true;
                }
            }
        });
    }
}