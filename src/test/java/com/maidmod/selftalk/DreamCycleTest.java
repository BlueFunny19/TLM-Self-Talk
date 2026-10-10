package com.maidmod.selftalk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DreamCycle} 的单元测试：一觉的生命周期、过期请求的丢弃、醒来后的汇报窗口。
 */
class DreamCycleTest {
    private static final long WINDOW = 300 * 20L;

    @Test
    void observe_reportsTransitionsOnlyWhenTheStateChanges() {
        DreamCycle cycle = new DreamCycle();
        assertEquals(DreamCycle.Transition.NONE, cycle.observe(false, 0));
        assertEquals(DreamCycle.Transition.FELL_ASLEEP, cycle.observe(true, 10));
        assertEquals(DreamCycle.Transition.NONE, cycle.observe(true, 11));
        assertEquals(DreamCycle.Transition.WOKE_UP, cycle.observe(false, 500));
        assertEquals(DreamCycle.Transition.NONE, cycle.observe(false, 501));
    }

    @Test
    void shouldGenerate_waitsUntilShesSleptLongEnough() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 100);
        cycle.decideDream(true);
        assertFalse(cycle.shouldGenerate(100));
        assertFalse(cycle.shouldGenerate(100 + DreamCycle.SETTLE_TICKS - 1));
        assertTrue(cycle.shouldGenerate(100 + DreamCycle.SETTLE_TICKS));
    }

    @Test
    void shouldGenerate_onlyOncePerSleep() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.decideDream(true);
        assertTrue(cycle.shouldGenerate(DreamCycle.SETTLE_TICKS));
        cycle.markRequested();
        assertFalse(cycle.shouldGenerate(DreamCycle.SETTLE_TICKS + 1));
        assertFalse(cycle.shouldGenerate(DreamCycle.SETTLE_TICKS * 10));
    }

    @Test
    void shouldGenerate_neverWhileAwake() {
        DreamCycle cycle = new DreamCycle();
        assertFalse(cycle.shouldGenerate(1_000_000));
        cycle.observe(true, 0);
        cycle.decideDream(true);
        cycle.observe(false, 10);
        assertFalse(cycle.shouldGenerate(1_000_000));
    }

    @Test
    void rollsDream_followsTheConfiguredPercentage() {
        assertFalse(DreamCycle.rollsDream(0, 0.0), "0% 永远不做梦");
        assertFalse(DreamCycle.rollsDream(0, 0.5));
        assertTrue(DreamCycle.rollsDream(100, 0.999999), "100% 永远做梦");
        assertTrue(DreamCycle.rollsDream(20, 0.19));
        assertFalse(DreamCycle.rollsDream(20, 0.20));
        assertFalse(DreamCycle.rollsDream(20, 0.99));
    }

    @Test
    void aSleepWithoutTheDreamRollNeverGeneratesOrWaits() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.decideDream(false);
        assertFalse(cycle.willDream());
        assertFalse(cycle.shouldGenerate(DreamCycle.SETTLE_TICKS * 10));
        assertFalse(cycle.awaitingDream(1), "没有梦的夜晚，梦话不必等梦");
    }

    @Test
    void decideDream_isIgnoredWhileAwakeAndResetOnEachSleep() {
        DreamCycle cycle = new DreamCycle();
        cycle.decideDream(true);
        assertFalse(cycle.willDream(), "醒着时抽签无效");
        cycle.observe(true, 0);
        cycle.decideDream(true);
        assertTrue(cycle.willDream());
        cycle.observe(false, 10);
        cycle.observe(true, 20);
        assertFalse(cycle.willDream(), "新的一觉要重新抽");
    }

    @Test
    void awaitingDream_holdsSleepTalkUntilTheDreamArrivesFailsOrTimesOut() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.decideDream(true);
        assertTrue(cycle.awaitingDream(10), "有梦的夜晚、梦还没来：梦话要等");
        assertTrue(cycle.awaitingDream(DreamCycle.AWAIT_LIMIT_TICKS - 1));
        assertFalse(cycle.awaitingDream(DreamCycle.AWAIT_LIMIT_TICKS), "等太久就不等了");

        DreamCycle arrived = new DreamCycle();
        arrived.observe(true, 0);
        arrived.decideDream(true);
        arrived.setDream(arrived.sleepId(), "梦");
        assertFalse(arrived.awaitingDream(10), "梦到了就不用等");

        DreamCycle failed = new DreamCycle();
        failed.observe(true, 0);
        failed.decideDream(true);
        failed.markFailed(failed.sleepId());
        assertFalse(failed.awaitingDream(10), "梦生成失败就不等了");
        assertFalse(failed.shouldGenerate(DreamCycle.SETTLE_TICKS), "失败了也不重试");
    }

    @Test
    void markFailed_ignoresAStaleSleepId() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        int old = cycle.sleepId();
        cycle.observe(false, 10);
        cycle.observe(true, 20);
        cycle.decideDream(true);
        cycle.markFailed(old);
        assertTrue(cycle.awaitingDream(30));
    }

    @Test
    void setDream_acceptsTheCurrentSleepAndTrims() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        assertTrue(cycle.setDream(cycle.sleepId(), "  梦见了蛋糕  "));
        assertEquals("梦见了蛋糕", cycle.dream());
        assertFalse(cycle.shouldGenerate(DreamCycle.SETTLE_TICKS), "有梦之后不再生成");
    }

    @Test
    void setDream_rejectsAStaleRequestFromAnEarlierSleep() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        int oldId = cycle.sleepId();
        cycle.observe(false, 100);
        cycle.observe(true, 200);
        assertFalse(cycle.setDream(oldId, "旧的梦"));
        assertNull(cycle.dream());
    }

    @Test
    void setDream_rejectsAfterShesAwakeOrBlank() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        int id = cycle.sleepId();
        assertFalse(cycle.setDream(id, "   "));
        assertFalse(cycle.setDream(id, null));
        cycle.observe(false, 50);
        assertFalse(cycle.setDream(id, "来晚了"));
        assertNull(cycle.dream());
    }

    @Test
    void fallingAsleepAgain_startsAFreshSleepWithoutTheOldDream() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.setDream(cycle.sleepId(), "旧的梦");
        cycle.observe(false, 100);
        cycle.observe(true, 200);
        cycle.decideDream(true);
        assertNull(cycle.dream());
        assertTrue(cycle.shouldGenerate(200 + DreamCycle.SETTLE_TICKS));
    }

    @Test
    void shouldReport_onlyAfterWakingWithAnUnreportedDreamInsideTheWindow() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.setDream(cycle.sleepId(), "梦");
        assertFalse(cycle.shouldReport(10, WINDOW), "还在睡");
        cycle.observe(false, 1000);
        assertTrue(cycle.shouldReport(1000, WINDOW));
        assertTrue(cycle.shouldReport(1000 + WINDOW, WINDOW));
        assertFalse(cycle.shouldReport(1000 + WINDOW + 1, WINDOW), "窗口过了就不说了");
        cycle.markReported();
        assertFalse(cycle.shouldReport(1001, WINDOW));
    }

    @Test
    void shouldReport_falseWithoutADream() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.observe(false, 1000);
        assertFalse(cycle.shouldReport(1001, WINDOW));
    }

    @Test
    void takeForWakeNote_returnsTheDreamOnceAndStopsTheSeparateReport() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        cycle.setDream(cycle.sleepId(), "梦");
        cycle.observe(false, 1000);
        assertEquals("梦", cycle.takeForWakeNote());
        assertNull(cycle.takeForWakeNote());
        assertFalse(cycle.shouldReport(1001, WINDOW));
    }

    @Test
    void takeForWakeNote_nullWithoutADream() {
        DreamCycle cycle = new DreamCycle();
        assertNull(cycle.takeForWakeNote());
        cycle.observe(true, 0);
        assertNull(cycle.takeForWakeNote());
    }

    @Test
    void reset_clearsEverythingAndInvalidatesInFlightRequests() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
        int id = cycle.sleepId();
        cycle.setDream(id, "梦");
        cycle.reset();
        assertNull(cycle.dream());
        assertFalse(cycle.isSleeping());
        assertFalse(cycle.setDream(id, "来晚了"));
    }
}
