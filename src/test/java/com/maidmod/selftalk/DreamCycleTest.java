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
        assertFalse(cycle.shouldGenerate(100));
        assertFalse(cycle.shouldGenerate(100 + DreamCycle.SETTLE_TICKS - 1));
        assertTrue(cycle.shouldGenerate(100 + DreamCycle.SETTLE_TICKS));
    }

    @Test
    void shouldGenerate_onlyOncePerSleep() {
        DreamCycle cycle = new DreamCycle();
        cycle.observe(true, 0);
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
        cycle.observe(false, 10);
        assertFalse(cycle.shouldGenerate(1_000_000));
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
