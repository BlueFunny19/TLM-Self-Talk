package com.maidmod.selftalk.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.maidmod.selftalk.Config;
import com.maidmod.selftalk.InterChatChain;
import com.maidmod.selftalk.MaidInterChatService;
import com.maidmod.selftalk.PlayerSettingsStore;
import com.maidmod.selftalk.SelfTalkDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * TLM Self-Talk 对外公开 API（供其他附属 Mod 在服务端调用）。
 * <p>
 * 本类只提供静态方法，<b>不保证二进制向后兼容</b>：内部实现变动时可能调整，
 * 调用方应以 {@link #API_VERSION} 与 {@link #isAvailable()} 做反射探测与降级。
 * <p>
 * 线程约定：所有会改动运行时状态的方法都必须在 <b>服务端主线程</b>调用。
 * 客户端/回调线程调用不会抛异常，只会返回 false（见 {@link #relayToMaid}）。
 */
public final class SelfTalkApi {

    /** 本 API 的版本号：发生不兼容变更时递增。当前为 1。 */
    public static final int API_VERSION = 1;

    private SelfTalkApi() {
    }

    /**
     * 供其他 Mod 反射探测本 API 是否可用，恒为 {@code true}。
     * <p>
     * 调用方宜先 {@code ModList.get().isLoaded("maid_self_talk")} 再经独立桥接类反射调用，
     * 避免在未安装本 Mod 时触发 {@code NoClassDefFoundError}。
     */
    public static boolean isAvailable() {
        return true;
    }

    /**
     * 让女仆 {@code from} 把一句话 {@code text} 转告女仆 {@code to}，
     * 并由 {@code to} 通过 Self Talk 的<b>互聊流程</b>生成回复；回复照常以气泡/广播形式显示给附近玩家。
     * <p>
     * 语义等价于「{@code from} 说了 {@code text}，{@code to} 接着回应」：{@code text} 会作为一条
     * assistant 消息注入 {@code to} 的请求上下文，随后是否继续往返由现有「连续概率 / 最大链长」
     * 配置决定，本 API 不做特殊处理。这是<b>被明确要求的转告</b>，不是随机触发，
     * 因此不再检查距离、冷却与全局限流闸门。
     * <p>
     * 必须在服务端主线程调用。返回 {@code true} 仅表示请求已被接受（已入队或已派发），
     * <b>不表示</b> {@code to} 已回复或回复成功。
     * <p>
     * 返回 {@code false} 的情形（全部安全拒绝，不抛异常、不遗留链/锁）：
     * <ul>
     *   <li>不在服务端主线程（含客户端侧）调用；</li>
     *   <li>{@code from}/{@code to} 为 null、为同一只、已死亡/已移除、不在同一维度，或 {@code text} 为空白；</li>
     *   <li>Self Talk 总开关（{@code general.enabled}）或女仆互聊开关（{@code inter_maid_chat.enabled}）关闭；</li>
     *   <li>允许玩家自定义配置时，任一方的主人已为该女仆关闭互聊（个人开关被尊重）；</li>
     *   <li>任一方已处于互聊链或互聊对锁中（不打断现有对话）；</li>
     *   <li>回答者 {@code to} 的 AI 前置不满足：无 AI 会话、LLM 总开关关闭、站点不可用、未配置人设；</li>
     *   <li>请求被派发闸门吞掉（顺延队列已满等）。</li>
     * </ul>
     *
     * @param from 转告方女仆（只提供内容，不需要在本 API 内生成请求）
     * @param to   回应方女仆（真正调用 LLM 生成回复）
     * @param text 转告内容；原样注入，空白内容拒绝
     * @return 请求是否已被受理
     */
    public static boolean relayToMaid(EntityMaid from, EntityMaid to, String text) {
        // 1) 基础参数校验：先判空，避免后续访问 NPE
        if (from == null || to == null || text == null || text.isBlank()) {
            return false;
        }
        Level fromLevel = from.level();
        Level toLevel = to.level();
        if (fromLevel == null || toLevel == null) {
            return false;
        }
        // 2) 侧别与线程：必须在服务端主线程；客户端或网络回调线程一律安全拒绝。
        //    server == null 即客户端侧（ClientLevel.getServer() 为 null）。
        MinecraftServer server = fromLevel.getServer();
        if (server == null || !server.isSameThread()) {
            return false;
        }
        // 3) 同一只、已死亡/已移除、跨维度
        if (from == to || from.getId() == to.getId()) {
            return false;
        }
        if (from.isRemoved() || to.isRemoved() || !from.isAlive() || !to.isAlive()) {
            return false;
        }
        if (fromLevel != toLevel) {
            return false;
        }
        // 4) 总开关与互聊开关（与随机触发同一组开关）
        if (!Config.ENABLED.get() || !Config.INTER_CHAT_ENABLED.get()) {
            return false;
        }
        // 5) 玩家个人开关：允许玩家配置时，任一方的主人关闭了互聊即拒绝。
        //    与随机触发/链式续接的判定口径一致（Config.PLAYER_OPTION_ENABLED）。
        if (Config.PLAYER_OPTION_ENABLED.get()
                && (!PlayerSettingsStore.isInterChatEnabledForMaid(server, from)
                || !PlayerSettingsStore.isInterChatEnabledForMaid(server, to))) {
            return false;
        }
        // 6) 已有互聊在进行：链登记与对锁任一命中即拒绝，绝不打断现有链
        if (InterChatChain.activeFor(from) != null || InterChatChain.activeFor(to) != null) {
            return false;
        }
        long nowTick = server.getTickCount();
        if (SelfTalkDispatcher.isMaidInterChatLocked(from, nowTick)
                || SelfTalkDispatcher.isMaidInterChatLocked(to, nowTick)) {
            return false;
        }
        // 7) service 层 AI 前置（沿用 MaidInterChatService 现有判断），不满足则不建链
        if (!MaidInterChatService.canTriggerInterChat(to)) {
            return false;
        }
        // 8) 建链并派发：chainRound=1 表示「本链第 1 条消息（from 的转告）之后 to 的第一次回应」，
        //    与 InterChatChain 对「首次交接不做 formalPhase 校验」的语义一致，不会让链提前结束或卡死。
        //    广播范围沿用现有互聊触发所用的 Config.INTER_CHAT_PLAYER_RANGE。
        InterChatChain chain = InterChatChain.create(from, to);
        boolean accepted;
        try {
            // 转告内容没有走过 Self Talk 的互聊窗口，给它一个新的消息身份：
            // 回答者回复成功后会按这个身份把转告的话同步进自己的互聊窗口，之后的对话里能记得对方说过什么
            accepted = SelfTalkDispatcher.submitInterChatResponder(to, from, UUID.randomUUID(), text,
                    Config.INTER_CHAT_PLAYER_RANGE.get(), 1, chain.id());
        } catch (Throwable t) {
            // 派发闸门内部异常：释放刚建的链，绝不让调用方看到异常
            InterChatChain.release(chain);
            return false;
        }
        if (!accepted) {
            // 被闸门吞掉（顺延队列已满等）：链未被任何请求持有，立即释放
            InterChatChain.release(chain);
            return false;
        }
        // 立即派发同步失败时 dispatchNow 已自行释放链；此时链不在注册表，视为未受理。
        // 已入队或已派发时链仍在注册表（由回调/清扫负责最终释放）。
        return InterChatChain.of(chain.id()) != null;
    }
}
