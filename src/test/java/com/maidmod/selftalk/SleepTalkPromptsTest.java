package com.maidmod.selftalk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 睡眠功能的两项纯逻辑自检：
 * <ul>
 *   <li>{@link SleepTalkPrompts} 三个方法中英版本都非空且互不相同（提示词不串用）；</li>
 *   <li>{@link Config#randomIntervalTicks} 的取值范围（梦话间隔沿用该随机口径）。</li>
 * </ul>
 * 不加载 Forge 配置实例，只调用纯静态方法（{@code Config} 的 SPEC 静态构建不依赖游戏运行时）。
 */
class SleepTalkPromptsTest {

    @Test
    void sleepTalkPromptsAreNonBlankInBothLanguages() {
        assertFalse(SleepTalkPrompts.dreamTalk(true).isBlank(), "中文梦话提示词不应为空");
        assertFalse(SleepTalkPrompts.dreamTalk(false).isBlank(), "英文梦话提示词不应为空");
        assertFalse(SleepTalkPrompts.mumbleReply(true).isBlank(), "中文嘟囔提示词不应为空");
        assertFalse(SleepTalkPrompts.mumbleReply(false).isBlank(), "英文嘟囔提示词不应为空");
        assertFalse(SleepTalkPrompts.wakeUpNote(true).isBlank(), "中文起床提示不应为空");
        assertFalse(SleepTalkPrompts.wakeUpNote(false).isBlank(), "英文起床提示不应为空");
    }

    @Test
    void sleepTalkPromptsDifferBetweenScenesAndLanguages() {
        // 同一语言内三段互不相同（梦话 / 嘟囔 / 起床提示各有语义，不能串用）
        assertNotEquals(SleepTalkPrompts.dreamTalk(true), SleepTalkPrompts.mumbleReply(true));
        assertNotEquals(SleepTalkPrompts.dreamTalk(true), SleepTalkPrompts.wakeUpNote(true));
        assertNotEquals(SleepTalkPrompts.mumbleReply(true), SleepTalkPrompts.wakeUpNote(true));
        assertNotEquals(SleepTalkPrompts.dreamTalk(false), SleepTalkPrompts.mumbleReply(false));
        assertNotEquals(SleepTalkPrompts.dreamTalk(false), SleepTalkPrompts.wakeUpNote(false));
        assertNotEquals(SleepTalkPrompts.mumbleReply(false), SleepTalkPrompts.wakeUpNote(false));
        // 中英版本也必须不同（否则说明语言分支写错）
        assertNotEquals(SleepTalkPrompts.dreamTalk(true), SleepTalkPrompts.dreamTalk(false));
        assertNotEquals(SleepTalkPrompts.mumbleReply(true), SleepTalkPrompts.mumbleReply(false));
        assertNotEquals(SleepTalkPrompts.wakeUpNote(true), SleepTalkPrompts.wakeUpNote(false));
    }

    @Test
    void dreamTalkIntervalStaysInsideConfiguredRange() {
        int minSeconds = 180;
        int maxSeconds = 600;
        for (int i = 0; i < 500; i++) {
            int ticks = Config.randomIntervalTicks(minSeconds, maxSeconds);
            assertTrue(ticks >= minSeconds * 20 && ticks <= maxSeconds * 20,
                    "梦话间隔必须落在 [min, max] 秒内，实际 " + ticks + " tick");
        }
    }

    @Test
    void dreamTalkIntervalHandlesEqualAndInvertedRange() {
        assertEquals(300 * 20, Config.randomIntervalTicks(300, 300), "MIN=MAX 时应恒为该值");
        for (int i = 0; i < 200; i++) {
            int ticks = Config.randomIntervalTicks(600, 180);
            assertTrue(ticks >= 180 * 20 && ticks <= 600 * 20,
                    "倒置配置应自动交换后仍落在区间内，实际 " + ticks + " tick");
        }
    }
}
