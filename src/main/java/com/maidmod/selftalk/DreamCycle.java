package com.maidmod.selftalk;

/**
 * 一只女仆「一觉」里与梦有关的状态（纯逻辑，不依赖 Minecraft／TLM 类型，可离线单测）。
 * <p>
 * 一觉从她睡着开始、到醒来结束，期间的流程是：
 * <ol>
 *   <li>睡着 {@link #SETTLE_TICKS} 之后生成这一觉的梦（{@link #shouldGenerate}），生成请求发出后不再重复发
 *       （{@link #markRequested}），失败了就这一觉没有梦——不重试，免得失败时每个 tick 都在请求；</li>
 *   <li>梦生成好之后保存下来（{@link #setDream}），梦话可以引用它（{@link #dream}）；</li>
 *   <li>醒来后的一段窗口内，若她是自然醒来，且梦还没说过，就把梦讲给主人（{@link #shouldReport}、
 *       {@link #markReported}）；被玩家叫醒的则改为把梦附在起床提示里（{@link #takeForWakeNote}）。</li>
 * </ol>
 * 每次睡着都会开启新的一觉（{@link #sleepId} 加一），晚到的旧请求结果按这个编号丢弃，
 * 不会把上一觉的梦写进这一觉。
 * <p>
 * 状态只在内存里，服务器重启后清零（重启时睡着的女仆会被当成刚睡着，重新做一个梦）。
 * 只在服务端主线程访问。
 */
public final class DreamCycle {
    /** 睡着多久之后才生成梦（tick）：小憩几秒钟就醒的不必为它请求一次模型。 */
    public static final long SETTLE_TICKS = 30 * 20L;

    /** {@link #observe} 看到的睡眠状态变化。 */
    public enum Transition {
        NONE,
        /** 刚刚睡着：开启新的一觉。 */
        FELL_ASLEEP,
        /** 刚刚醒来。 */
        WOKE_UP
    }

    private boolean sleeping;
    private int sleepId;
    private long fellAsleepTick = -1;
    private long wokeTick = -1;
    private boolean requested;
    private boolean reported;
    private String dream;

    /**
     * 每个处理周期用女仆此刻是否在睡觉更新一次。
     *
     * @return 这次相对上次的变化
     */
    public Transition observe(boolean nowSleeping, long nowTick) {
        if (nowSleeping == sleeping) {
            return Transition.NONE;
        }
        sleeping = nowSleeping;
        if (nowSleeping) {
            sleepId++;
            fellAsleepTick = nowTick;
            wokeTick = -1;
            requested = false;
            reported = false;
            dream = null;
            return Transition.FELL_ASLEEP;
        }
        wokeTick = nowTick;
        return Transition.WOKE_UP;
    }

    /** 这一觉的编号：每次睡着加一，请求发出时带上它，结果回来时用它判断是不是已经过期。 */
    public int sleepId() {
        return sleepId;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    // ===== 生成 =====

    /** 现在该不该发出生成梦的请求：睡着、睡够 {@link #SETTLE_TICKS}、这一觉还没请求过。 */
    public boolean shouldGenerate(long nowTick) {
        return sleeping && !requested && dream == null
                && fellAsleepTick >= 0 && nowTick - fellAsleepTick >= SETTLE_TICKS;
    }

    /** 已经发出（或决定放弃）这一觉的生成请求：这一觉不再请求。 */
    public void markRequested() {
        requested = true;
    }

    /**
     * 生成的结果回来了。
     *
     * @param forSleepId 请求发出时的 {@link #sleepId}
     * @return 是否收下了（不是这一觉的、她已经醒了、内容为空，都不收）
     */
    public boolean setDream(int forSleepId, String text) {
        if (forSleepId != sleepId || !sleeping || text == null || text.isBlank()) {
            return false;
        }
        dream = text.trim();
        return true;
    }

    /** 这一觉的梦（还没有、已被清掉时为 {@code null}）。 */
    public String dream() {
        return dream;
    }

    // ===== 醒来后 =====

    /**
     * 醒来后该不该把梦讲给主人：已经醒了、有梦、没讲过、还在 {@code windowTicks} 窗口内。
     * 被叫醒的情况由调用方先 {@link #takeForWakeNote} 取走，这里就不会再返回 true。
     */
    public boolean shouldReport(long nowTick, long windowTicks) {
        return !sleeping && dream != null && !reported && wokeTick >= 0 && nowTick - wokeTick <= windowTicks;
    }

    /** 梦已经讲过了（或决定不再讲）。 */
    public void markReported() {
        reported = true;
    }

    /**
     * 被玩家叫醒时取走梦，附在起床提示里。已经醒了的（自然醒来后才被叫到）也可以取。
     *
     * @return 梦的内容；没有梦或已经说过时为 {@code null}
     */
    public String takeForWakeNote() {
        if (dream == null || reported) {
            return null;
        }
        reported = true;
        return dream;
    }

    /** 清掉全部状态（女仆被清空记忆、死亡等）。 */
    public void reset() {
        sleeping = false;
        fellAsleepTick = -1;
        wokeTick = -1;
        requested = false;
        reported = false;
        dream = null;
        sleepId++;
    }
}
