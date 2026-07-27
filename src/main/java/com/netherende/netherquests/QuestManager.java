package com.netherende.netherquests;

import java.util.ArrayList;
import java.util.List;

public class QuestManager {
    private static final List<Chapter> chapters = new ArrayList<>();
    private static final List<Quest> quests = new ArrayList<>();
    public static String currentChapterId = "";
    
    // Hier ist die neue Variable richtig platziert!
    public static String pinnedQuestId = "";

    public static void init() {
        if (chapters.isEmpty()) {
            chapters.add(new Chapter("main_chapter", "Der Nether", ""));
            currentChapterId = "main_chapter";
        }
    }

    public static List<Chapter> getChapters() { return chapters; }
    public static List<Quest> getQuests() { return quests; }

    public static void addChapter(Chapter chapter) {
        chapters.add(chapter);
        if (currentChapterId.isEmpty()) {
            currentChapterId = chapter.id;
        }
    }

    public static void deleteChapter(String chapterId) {
        chapters.removeIf(c -> c.id.equals(chapterId) || c.parentId.equals(chapterId));
        quests.removeIf(q -> q.chapterId.equals(chapterId));
        if (currentChapterId.equals(chapterId)) {
            currentChapterId = chapters.isEmpty() ? "" : chapters.get(0).id;
        }
    }

    public static void addQuest(Quest quest) {
        quests.add(quest);
    }

    public static Quest getQuestById(String id) {
        for (Quest q : quests) {
            if (q.id.equals(id)) return q;
        }
        return null;
    }
}