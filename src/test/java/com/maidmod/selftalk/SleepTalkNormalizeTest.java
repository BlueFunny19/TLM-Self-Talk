package com.maidmod.selftalk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MaidSleepTalkService#normalizeText} 的单元测试：引号、换行、截断，以及剥掉其它 Mod 约定的停顿标记。
 */
class SleepTalkNormalizeTest {

    @Test
    void stripsPairedQuotesAndCollapsesLineBreaks() {
        assertEquals("嗯……蛋糕", MaidSleepTalkService.normalizeText("“嗯……蛋糕”", 40));
        assertEquals("a b", MaidSleepTalkService.normalizeText("a\nb", 40));
    }

    @Test
    void truncatesToTheLimit() {
        assertEquals("12345", MaidSleepTalkService.normalizeText("1234567890", 5));
        assertEquals("", MaidSleepTalkService.normalizeText(null, 5));
    }

    @Test
    void removesPauseMarkersFromTheText() {
        assertEquals("呼…… 蛋糕", MaidSleepTalkService.normalizeText("呼……<等待>蛋糕", 40));
        assertEquals("zzz mm", MaidSleepTalkService.normalizeText("zzz<wait 2s>mm", 40));
        assertEquals("好困", MaidSleepTalkService.normalizeText("<等待>好困<等待>", 40));
    }

    @Test
    void leavesOrdinaryAngleBracketsAlone() {
        assertEquals("a < b", MaidSleepTalkService.normalizeText("a < b", 40));
        assertEquals("<waiting>", MaidSleepTalkService.normalizeText("<waiting>", 40));
    }
}
