package com.netherende.netherquests;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    public String id;
    public String chapterId;
    public String title;
    public String description;
    public int x, y;
    
    // Bedingungen: ITEM, LEVEL, ADVANCEMENT, MOB_KILL, LOCATION
    public String reqType; 
    public String requiredItem; // Item-ID, Mob-ID (z.B. "minecraft:blaze") oder Biom-ID
    public int requiredAmount;
    
    // Belohnung: ITEM, LEVEL, ADVANCEMENT, COMMAND, CHOICE
    public String rewardType;
    public String rewardItem;
    public int rewardAmount;
    public List<String> choiceRewards = new ArrayList<>(); // Fuer CHOICE-Belohnungen
    public int selectedChoiceIndex = 0;

    public List<String> dependencies = new ArrayList<>();
    public String iconItem;
    
    // Zustand
    public boolean isCompleted = false;
    public boolean isPinned = false;
    public int currentProgress = 0;
    public boolean notifiedReady = false;

    public Quest(String id, String chapterId, String title, String description, int x, int y, 
                 String reqType, String requiredItem, int requiredAmount, 
                 String rewardType, String rewardItem, int rewardAmount, 
                 List<String> choiceRewards, List<String> dependencies, String iconItem) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.description = description != null ? description : "";
        this.x = x;
        this.y = y;
        this.reqType = reqType != null ? reqType : "ITEM";
        this.requiredItem = requiredItem;
        this.requiredAmount = requiredAmount;
        this.rewardType = rewardType != null ? rewardType : "ITEM";
        this.rewardItem = rewardItem;
        this.rewardAmount = rewardAmount;
        if (choiceRewards != null) this.choiceRewards = choiceRewards;
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
        this.iconItem = iconItem;
    }

    public boolean isReadyToClaim() {
        if (isCompleted) return false;
        return currentProgress >= requiredAmount;
    }
}