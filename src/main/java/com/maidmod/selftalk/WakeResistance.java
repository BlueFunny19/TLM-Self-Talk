package com.maidmod.selftalk;

/**
 * 「叫不醒」连叫判定的纯逻辑（不依赖 Minecraft／TLM 类型，可离线单测）。
 * <p>
 * 语义（与配置 {@code sleep.wake*} 一致）：
 * <ol>
 *   <li>每次「一轮连叫」开始时随机取 N ∈ [WAKE_MIN_CALLS, WAKE_MAX_CALLS]（含两端）；</li>
 *   <li>第 1..N-1 次叫：返回 {@link Decision#MUMBLE}（只嘟囔，不醒来、不回答内容）；</li>
 *   <li>第 N 次叫：返回 {@link Decision#WAKE}（叫醒她），同时把记录清零，
 *       下次叫她即为新一轮（重新随机 N）；</li>
 *   <li>距上一次叫超过 {@code WAKE_STREAK_RESET_SECONDS} 秒：先清零再计数，
 *       视为新一轮（超时后重新随机 N）。</li>
 * </ol>
 * <p>
 * 记录是可变对象（{@link Calls}），由调用方按女仆存在 {@link SelfTalkState.State} 里，
 * 只在服务端主线程访问。<b>本类不持有任何全局状态</b>，因此多只女仆天然互不影响。
 * <p>
 * 随机源以 {@code roll01}（[0,1) 的随机数）显式传入，便于单测确定性地覆盖边界。
 */
public final class WakeResistance {

    private WakeResistance() {
    }

    /** 本次叫她的结果：继续迷糊嘟囔，或已被叫醒 */
    public enum Decision { MUMBLE, WAKE }

    /**
     * 一只女仆的连叫记录（可变，仅服务端主线程访问）。
     * <p>
     * {@code lastCallTick} 用 {@link Long#MIN_VALUE} 表示「尚无记录」，
     * 与服务器 tick 的实际取值不可能冲突（{@code server.getTickCount()} 恒为非负且不会接近该值）。
     */
    public static final class Calls {
        /** 本轮已叫次数 */
        private int calls;
        /** 本轮需要叫醒她的次数 N（0 = 尚未抽取） */
        private int needed;
        /** 上一次叫她的服务器 tick */
        private long lastCallTick = Long.MIN_VALUE;

        public int calls() {
            return calls;
        }

        public int needed() {
            return needed;
        }

        public long lastCallTick() {
            return lastCallTick;
        }

        /** 清零（被叫醒、超时、女仆重新入睡时调用） */
        public void reset() {
            calls = 0;
            needed = 0;
            lastCallTick = Long.MIN_VALUE;
        }
    }

    /**
     * 处理一次「玩家叫熟睡的女仆」。
     *
     * @param calls        该女仆的连叫记录（就地更新）
     * @param nowTick      当前服务器 tick（全局单调，与 {@code server.getTickCount()} 同基准）
     * @param minCalls     本轮最少次数（含）
     * @param maxCalls     本轮最多次数（含）
     * @param resetSeconds 超过该时长未叫她即清零重来（秒）
     * @param roll01       随机源，[0,1)
     * @return {@link Decision#MUMBLE} 继续迷糊嘟囔；{@link Decision#WAKE} 叫醒她（记录已清零）
     */
    public static Decision onPlayerCall(Calls calls, long nowTick, int minCalls, int maxCalls,
                                        int resetSeconds, double roll01) {
        // 超时清零：必须发生在计数之前，否则旧计数会带到新一轮
        if (calls.lastCallTick != Long.MIN_VALUE
                && nowTick - calls.lastCallTick > resetSeconds * 20L) {
            calls.reset();
        }
        if (calls.needed <= 0) {
            calls.needed = rollNeeded(minCalls, maxCalls, roll01);
        }
        calls.calls++;
        calls.lastCallTick = nowTick;
        if (calls.calls >= calls.needed) {
            // 被叫醒：本轮结束，记录清零（下一次叫她重新随机 N）
            calls.reset();
            return Decision.WAKE;
        }
        return Decision.MUMBLE;
    }

    /**
     * 在 [minCalls, maxCalls] 内随机取整数 N（含两端）。
     * <p>
     * 与 {@link Config#randomIntervalTicks} 同一套防御：max &lt; min 时交换，
     * 小于 1 的配置值收敛到 1（配置项本身已有范围校验，这里只兜底手改 toml 的极端值）。
     */
    static int rollNeeded(int minCalls, int maxCalls, double roll01) {
        if (maxCalls < minCalls) {
            int temp = minCalls;
            minCalls = maxCalls;
            maxCalls = temp;
        }
        if (minCalls < 1) {
            minCalls = 1;
        }
        if (maxCalls < 1) {
            maxCalls = 1;
        }
        double roll = roll01 < 0.0 ? 0.0 : (roll01 >= 1.0 ? 0.9999999999d : roll01);
        return minCalls + (int) (roll * (maxCalls - minCalls + 1));
    }
}
