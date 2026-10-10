package com.maidmod.selftalk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 「叫不醒」连叫判定的纯逻辑单测（{@link WakeResistance}，不依赖 Minecraft／TLM）。
 * <p>
 * 覆盖：首次叫、N 的取值范围（含 MIN=MAX、MIN=1、倒置配置）、超时清零、
 * 醒来后清零、多只女仆互不影响。
 */
class WakeResistanceTest {

    /** 一秒的 tick 数（判定内部按 tick 计时） */
    private static final long TICKS_PER_SECOND = 20L;

    @Test
    void firstCallStartsStreakAndMumbles() {
        WakeResistance.Calls calls = new WakeResistance.Calls();
        WakeResistance.Decision decision = WakeResistance.onPlayerCall(calls, 0, 3, 3, 120, 0.0);
        assertEquals(WakeResistance.Decision.MUMBLE, decision);
        assertEquals(1, calls.calls(), "首次叫应记为第 1 次");
        assertEquals(3, calls.needed(), "首次叫应随机出本轮 N");
    }

    @Test
    void wakesOnNthCallAndResetsStreak() {
        WakeResistance.Calls calls = new WakeResistance.Calls();
        assertEquals(WakeResistance.Decision.MUMBLE, WakeResistance.onPlayerCall(calls, 0, 2, 2, 120, 0.0));
        assertEquals(WakeResistance.Decision.WAKE, WakeResistance.onPlayerCall(calls, 10, 2, 2, 120, 0.0));
        assertEquals(0, calls.calls(), "被叫醒后连叫计数应清零");
        assertEquals(0, calls.needed(), "被叫醒后本轮 N 应清零（下次重新随机）");
        // 醒来后重新开始：若 N 再随到 2，则下一次又叫只嘟囔
        assertEquals(WakeResistance.Decision.MUMBLE, WakeResistance.onPlayerCall(calls, 20, 2, 2, 120, 0.0));
        assertEquals(1, calls.calls());
    }

    @Test
    void minCallsOneWakesOnFirstCall() {
        WakeResistance.Calls calls = new WakeResistance.Calls();
        assertEquals(WakeResistance.Decision.WAKE, WakeResistance.onPlayerCall(calls, 0, 1, 1, 120, 0.0));
        assertEquals(0, calls.calls(), "第一次就叫醒时记录同样清零");
    }

    @Test
    void rollNeededCoversBothEndsAndInvertedConfig() {
        assertEquals(2, WakeResistance.rollNeeded(2, 5, 0.0), "roll=0 应取区间下限");
        assertEquals(5, WakeResistance.rollNeeded(2, 5, 0.999999), "roll→1 应取区间上限");
        assertEquals(7, WakeResistance.rollNeeded(7, 7, 0.42), "MIN=MAX 时应恒取该值");
        // 倒置配置（管理员手改 toml）自动交换，不产生区间外结果
        assertEquals(2, WakeResistance.rollNeeded(5, 2, 0.0));
        assertEquals(5, WakeResistance.rollNeeded(5, 2, 0.999999));
        // 小于 1 的极端值收敛到 1
        assertEquals(1, WakeResistance.rollNeeded(0, 0, 0.5));
        assertEquals(1, WakeResistance.rollNeeded(-3, -1, 0.5));
    }

    @Test
    void rollNeededStaysInsideRangeForManyRolls() {
        for (int i = 0; i < 1000; i++) {
            double roll = i / 1000.0;
            int needed = WakeResistance.rollNeeded(2, 5, roll);
            assertTrue(needed >= 2 && needed <= 5, "N 必须落在 [2, 5] 内，实际 " + needed);
        }
    }

    @Test
    void streakTimesOutAndStartsOver() {
        WakeResistance.Calls calls = new WakeResistance.Calls();
        assertEquals(WakeResistance.Decision.MUMBLE, WakeResistance.onPlayerCall(calls, 0, 3, 3, 120, 0.0));
        assertEquals(1, calls.calls());
        // 仍在窗口内（恰好等于阈值不算超过）：计数继续累加
        long withinWindowTick = 120 * TICKS_PER_SECOND;
        assertEquals(WakeResistance.Decision.MUMBLE,
                WakeResistance.onPlayerCall(calls, withinWindowTick, 3, 3, 120, 0.0));
        assertEquals(2, calls.calls(), "窗口内应继续累加");
        // 超过窗口：先清零再计数，因此记为新一轮的第 1 次，N 重新随机
        long afterWindowTick = withinWindowTick + 120 * TICKS_PER_SECOND + 1;
        assertEquals(WakeResistance.Decision.MUMBLE,
                WakeResistance.onPlayerCall(calls, afterWindowTick, 3, 3, 120, 0.0));
        assertEquals(1, calls.calls(), "超时后应清零并作为新一轮第 1 次");
        assertEquals(3, calls.needed());
    }

    @Test
    void timeoutDoesNotWakeEvenIfCallsExceededNeeded() {
        // N=5：叫了 4 次后超时清零；随后第 5 次叫只算新一轮第 1 次，不应被叫醒
        WakeResistance.Calls calls = new WakeResistance.Calls();
        for (int i = 0; i < 4; i++) {
            assertEquals(WakeResistance.Decision.MUMBLE,
                    WakeResistance.onPlayerCall(calls, i * 20L, 5, 5, 60, 0.0));
        }
        assertEquals(4, calls.calls());
        long afterTimeout = 4 * 20L + 60 * TICKS_PER_SECOND + 1;
        assertEquals(WakeResistance.Decision.MUMBLE,
                WakeResistance.onPlayerCall(calls, afterTimeout, 5, 5, 60, 0.0));
        assertEquals(1, calls.calls());
    }

    @Test
    void multipleMaidsAreIndependent() {
        WakeResistance.Calls maidA = new WakeResistance.Calls();
        WakeResistance.Calls maidB = new WakeResistance.Calls();
        assertEquals(WakeResistance.Decision.MUMBLE, WakeResistance.onPlayerCall(maidA, 0, 3, 3, 120, 0.0));
        assertEquals(WakeResistance.Decision.MUMBLE, WakeResistance.onPlayerCall(maidA, 5, 3, 3, 120, 0.0));
        assertEquals(WakeResistance.Decision.WAKE, WakeResistance.onPlayerCall(maidB, 10, 1, 1, 120, 0.0));
        assertEquals(2, maidA.calls(), "另一只女仆被叫醒不应影响本女仆的连叫计数");
        assertEquals(0, maidB.calls());
        assertNotEquals(maidA.calls(), maidB.calls());
    }

    @Test
    void resetClearsEverything() {
        WakeResistance.Calls calls = new WakeResistance.Calls();
        WakeResistance.onPlayerCall(calls, 0, 4, 4, 120, 0.0);
        calls.reset();
        assertEquals(0, calls.calls());
        assertEquals(0, calls.needed());
        assertEquals(Long.MIN_VALUE, calls.lastCallTick());
    }
}
