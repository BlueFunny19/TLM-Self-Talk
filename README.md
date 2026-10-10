# TLM Self-Talk

> [!WARNING]
> AI 辅助开发声明
>
> 本项目的 `forge-1.20.1` 分支（即本分支）是在原项目基础上，借助 AI 以 vibe coding 的方式添加定制功能的版本；
>
> 辅助开发使用的模型为 Claude Sonnet 5.5 和 DeepSeek V4.1 Flash；
>
> 代码经过人工逐行校对，但作者水平有限，仍然存在风险（可能有未被发现的错误、与其他模组的兼容问题等）；
>
> 如果你介意，请不要使用本分支

> 建议搭配改版的 [TLM-Sincerely](https://github.com/BlueFunny19/TLM-Sincerely)（车万女仆：真心为你，`1.20.1forge` 分支）一起使用；
>
> 它的「转告」工具通过本分支提供的 `SelfTalkApi` 让女仆之间互相传话；
>
> 不搭配也可以正常使用本分支，只是少了这项联动；
>
> 文中标有「**改版新增**」「**改版变更**」的条目，就是本分支相对原版新增或修改的内容

给 [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid)（车万女仆）添加「自言自语 / 欢迎语 / 互相对话」能力的附属 Mod。

## 功能

- **女仆自言自语**：主人在线时会随机开口，主人离线但附近有其他玩家时同样会触发
- **欢迎语**：主人上线时女仆主动打招呼，不限距离，已加载区块中的女仆都能触发
- **女仆互相对话**：女仆会随机地与其附近的女仆聊天
- **无人设不说话**：未配置人设的女仆不触发，避免出戏/不可预计的 Token 消耗
- **上下文控制**：自言自语保留条数可配置，当超过条数上限时会自动丢弃前面的自言自语内容，仅保留最后一条。玩家与女仆的主动聊天记录不受影响
- **随机注入游戏情境**：触发自言自语时，会从女仆当前位置、附近实体、装备等情境信息中随机注入几种信息，使自言自语具有随机性
- **自定义 Prompt**：可为自己的女仆自定义触发自言自语/互聊时的提示词，支持全部女仆设置与单只女仆独立设置
- **Tool 调用**：默认关闭，开启后，女仆可能在自言自语/互聊中自行切换状态、获取环境信息等。**会带来额外的token消耗**
- **单独开关**：每位玩家可单独关闭某只女仆的自言自语；房主可控制总开关（服务器则需要修改配置文件）
- **触发冷却**：可配置的随机触发间隔区间
- **睡觉时安静**：允许让女仆睡觉时保持安静，不触发自言自语/互聊
- **梦话（呓语）**（**改版新增**）：女仆熟睡时会按随机间隔自发说一句梦话（由 LLM 按人设生成），显示为气泡与聊天栏消息
- **做梦**（**改版新增**）：女仆熟睡一会儿后，LLM 会按人设、结合她和玩家之间已有的对话编出这一觉的梦；梦话会取材于它，自己睡醒后还会把梦讲给主人
- **叫不醒**（**改版新增**）：玩家对熟睡的女仆说话时，前几次她只迷迷糊糊嘟囔（不回答、不醒来），被叫够随机次数后才醒来正常回复
- **聊天可见**：女仆的自言自语与互聊对附近玩家可见（即使此玩家不是女仆的主人）
- **对外 API**（**改版新增**）：`SelfTalkApi.relayToMaid` 让其他 Mod 在服务端让一只女仆把话转告另一只女仆，并由后者走互聊流程回应（见下文「给其他 Mod 的 API」）

## 环境要求

| 加载器 | Minecraft | Java | Touhou Little Maid |
| --- | --- | --- | --- |
| NeoForge | 1.21.1 | 21 | 1.5.3 |
| Forge | 1.20.1 | 17 | 1.5.3 |

## 安装

1. 安装 mod：下载对应版本的 jar
2. 启动游戏，打开总开关：女仆 AI 设置 → AI 全局设置 →「女仆自言自语」→ 启用；「女仆互聊」需在其子页单独启用

## 使用

### 配置

女仆 AI 聊天设置 → AI 全局设置 →「女仆自言自语」：

- **状态 1 / 状态 2**：主人在线 / 离线时的开关、最小与最大触发间隔（秒，填入式、带范围校验）、玩家半径、自话保留条数
- **欢迎语**：主人上线后的打招呼触发窗口

女仆 AI 聊天设置 → AI 全局设置 →「女仆互聊」：

- **基础**：总开关（默认关闭）、最小与最大触发间隔（秒）、玩家距离（格）、女仆间距离（格）
- **上下文与连续对话**：保留轮数（问/答算一轮，超限后仅保留最近一条消息）、连续概率（每轮回答后按此概率继续）、最大链长（一次互聊的消息条数上限，连续概率拉满时的兜底）

女仆 AI 聊天设置 → AI 全局设置 →「女仆自言自语」→「睡眠」（**改版新增**）：

- **梦话**：开关、最小/最大间隔（秒）、触发半径（格，0 表示不要求有人在场）
- **做梦**：总开关、做梦概率（%）、梦话是否取材于梦、醒来后是否讲给主人、讲梦窗口（秒）、讲梦的话是否写进聊天历史
- **叫不醒**：开关、最少/最多叫醒次数（含两端）、连叫清零时长（秒）、醒后保持清醒时长（秒）

### 玩家独立设置

对着女仆打开 AI 聊天输入界面（通常是按 T 键），点左侧 💬 按钮打开女仆设置：

- **自言自语 / 互聊**：支持单独关闭自言自语/互聊
- **睡觉安静**：开启后，女仆睡觉时不再触发自言自语/互聊
- **自定义 Prompt**：支持为「全部女仆」和「这只女仆」分别填写提示词
- **Tool调用**：全局与单只开关，开启后这只女仆的自言自语/互聊可以调用工具

### 睡眠：梦话与叫不醒（改版新增）

女仆熟睡时（TLM 夜间日程上床睡觉）有两种新的表现：

- **梦话（呓语）**：按每只女仆各自的随机间隔（默认 180~600 秒）自发说一句梦话，由 LLM 按人设生成，
  显示为头顶气泡与聊天栏消息，附近玩家可见。
  - 开关 `sleep.enabled`（默认开）；最小/最大间隔 `sleep.minIntervalSeconds` / `sleep.maxIntervalSeconds`（30~7200 秒，两者倒置时自动交换）。
  - 触发半径 `sleep.playerRange`（默认 16 格）：半径内有玩家时才说（节省 Token）；`0` 表示不要求有人在场，此时聊天栏广播半径回落到 16 格。
  - **与「睡觉时安静」的关系**：「睡觉安静」只拦自言自语/互聊/欢迎语，梦话由本开关独立控制——即使开启睡觉安静，熟睡的女仆仍可能说梦话。梦话同样受总开关与 LLM 前置（LLM 总开关、站点可用、女仆有人设）约束。
- **做梦**：不是每个晚上都有梦——每只女仆每次睡着时按 `sleep.dreamChancePercent`（默认 20%，0~100）抽一次，抽中才有这一觉的梦。
  有梦的话，睡着约 30 秒后，LLM 会按人设、结合她和玩家之间已有的对话（消息前缀里本来就有，不额外读取别的数据），
  编出这一觉做的梦：场景可以跳跃荒诞，但取材于白天的人和事，第一人称、两三句话。梦只存在内存里，**不显示、不写入任何历史**，
  服务器重启后清零。每一觉最多一次请求，失败则这一觉没有梦（不重试）；小憩几秒就醒的不会为它请求。
  - 总开关 `sleep.dreamEnabled`（默认开），关闭则下面全部不生效。与梦话同口径：`sleep.playerRange > 0` 时半径内有玩家才生成。
  - **梦话取材于梦** `sleep.dreamTalkReferencesDream`（默认开）：有梦的夜晚里，她说的梦话都和梦有关——从梦里取一个片段说一句，梦生成出来之前先不说（最多等 3 分钟）；没有梦的夜晚照旧说泛泛的呓语。
  - **醒来后讲给主人** `sleep.dreamReportEnabled`（默认开）：自己睡醒（没有人叫）的女仆，在主人 16 格内、且在 `sleep.dreamReportWindowSeconds`
    （默认 300 秒）窗口内时，用一两句话说起这个梦（每个梦最多一次，多一次 LLM 请求，头顶气泡 + 聊天栏）；
    玩家单独关闭了这只女仆的自言自语时不讲。被玩家叫醒的梦不另发请求，而是附在起床提示后面，让她回答时可以顺口提一句。
  - **讲梦的话是否写进聊天历史** `sleep.dreamWriteHistory`（默认关）：关闭时这句话只用于显示，不进入主模组历史，不影响之后的对话与记忆整理；
    开启后她会在之后的对话里“记得”自己说过这个梦。梦本身从不写入历史。
  - 提示词（生成梦、取材梦话、讲梦、起床提示里的梦）都在 `SleepTalkPrompts`，中英各一套。
- **叫不醒**：玩家对熟睡的女仆说话时，前几次她不会正常回复，只迷迷糊糊嘟囔一句（不回答玩家说的话、不醒来）；
  被叫够随机次数 N 后才醒来并正常回复，醒来后的第一次回复会带上刚起床的状态
  （迷糊 / 撒娇 / 起床气 / 温柔由她的人设自己决定，代码里不写死台词）。
  - 开关 `sleep.wakeResistanceEnabled`（默认开）。
  - `sleep.wakeMinCalls` / `sleep.wakeMaxCalls`（默认 2 / 5，范围 1~20，含两端）：每次睡眠中的连叫开始时在 [最小, 最大] 内随机 N；
    第 1..N-1 次只嘟囔，第 N 次醒来。`1` 表示第一次就叫醒。
  - `sleep.wakeStreakResetSeconds`（默认 120 秒）：距上一次叫她超过该时长，连叫计数清零，下一次叫她视为新一轮并重新随机 N。
  - `sleep.wakeStayAwakeSeconds`（默认 120 秒，0 表示不处理）：被叫醒后这段时间内她不会再走向床睡觉，避免夜间日程下刚被叫醒就立刻睡回去。
  - **「叫不醒」会拦截玩家聊天入口**（TLM 的 `MaidAIChatManager#chat`）：嘟囔的那几次本次请求被取消，
    因此不会向 LLM 发送玩家那句话、不产生 token 消耗、也不写入聊天记录；玩家不会看到「思考中」提示或任何报错。
    只拦截「真玩家、且该女仆的主人就是他或他在女仆 8 格内」的调用，其余（包括其它 Mod 通过 `chat()` 发起的内部调用）一律放行。
- **Token 消耗提示**：梦话每次消耗一次 LLM 请求；做梦只在抽中的那一觉里发生，每觉一次（生成梦）、讲梦最多一次；「叫不醒」每次嘟囔消耗一次 LLM 请求
  （被叫醒的那一次是正常的玩家聊天请求）。LLM 不可用（未开启 AI、无可用站点）时梦话不触发、嘟囔静默不回复（仍然计数）。

### 可选：AgentTweaks 集成（Forge 1.20.1）

- 不需要安装 AgentTweaks：在 AI 聊天输入界面点 💬 即可打开女仆设置，关闭后返回聊天界面。
- 本版本提供设置导航桥；安装并接入该桥的 Forge 版 AgentTweaks 后，可由其提供统一导航入口，关闭女仆设置时返回父界面。
- 切换设置页前会保存自定义 Prompt；环境上下文浮层打开时不允许切页。
- 不新增网络协议或强制依赖。仅提供本侧接口，不代表 AgentTweaks 的 Forge 接入已完成。

### 给其他 Mod 的 API（改版新增）

本 Mod 提供一个供其他附属 Mod 在服务端调用的最小公开 API：`com.maidmod.selftalk.api.SelfTalkApi`（仅静态方法，`API_VERSION = 1`，可用 `isAvailable()` 反射探测）。

```java
// 服务端主线程调用：让 from 把 text 转告 to，并由 to 走互聊流程回应
EntityMaid from = ...;
EntityMaid to = ...;
boolean accepted = SelfTalkApi.relayToMaid(from, to, "主人让我告诉你，晚饭已经准备好了。");
```

- **线程**：必须在服务端主线程调用；客户端 / 网络回调线程调用只会返回 `false`，不会崩溃。
- **返回值**：`true` 仅表示请求已被接受（已入队或已派发），不代表 `to` 已经回复。
- **拒绝条件**：任一参数为 `null`、同一只女仆、已死亡 / 已移除、不在同一维度、文本为空白；Self Talk 总开关或「女仆互聊」开关关闭；任一方的主人已为该女仆关闭互聊；任一方已处于互聊链 / 对锁中（不会打断现有对话）；`to` 无 AI、LLM 总开关关闭、站点不可用或未配置人设。
- **不做检查**：这是「被明确要求的转告」，不检查距离、冷却与全局限流；单次对话的后续往返沿用现有「连续概率 / 最大链长」配置。
- **可见性**：`to` 的回复照常显示气泡并广播给附近玩家；`text` 本身不由本 API 广播。
- **依赖建议**：`compileOnly` + `mods.toml` 可选依赖（`mandatory = false`），并放在独立桥接类里反射调用（可参考本仓库对 AgentTweaks 的处理），避免把本 Mod 变成硬前置。

## 许可

[MIT](LICENSE)

---

# TLM Self-Talk (English)

> [!WARNING]
> AI-assisted development notice
>
> The `forge-1.20.1` branch (this branch) adds custom features on top of the original project through AI-assisted vibe coding.
>
> The models used for development are Claude Sonnet 5.5 and DeepSeek V4.1 Flash.
>
> The code was proofread line by line by a human, but the author's skills are limited, so risks remain (undiscovered bugs, compatibility issues with other mods, and so on).
>
> If that matters to you, please do not use this branch.

> Best used together with the modified [TLM-Sincerely](https://github.com/BlueFunny19/TLM-Sincerely) (branch `1.20.1forge`).
>
> Its `tell_maid` tool uses the `SelfTalkApi` provided by this branch so maids can pass words on to each other.
>
> This branch also works on its own, you just lose that link.
>
> Items marked **added in this fork** / **changed in this fork** are what this branch adds or modifies compared with the original.

A mod that adds **self-talk / welcome greetings / maid-to-maid chats** to [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid) maids.

## Features

- **Maid self-talk**: The maid randomly talks when her owner is online, and also triggers when the owner is offline but other players are nearby
- **Welcome greetings**: The maid greets her owner when they log in — no distance limit; any maid in loaded chunks can trigger
- **Maid-to-maid chat**: A maid will randomly strike up a conversation with maids near her
- **No persona, no talking**: Maids without a persona never trigger, avoiding out-of-character replies and unpredictable token cost
- **Context control**: The number of self-talk entries kept in context is configurable; once the limit is exceeded, earlier self-talk entries are dropped and only the latest one remains. Player-initiated chat history with the maid is unaffected
- **Random in-game context**: When triggering self-talk, a few pieces of context such as current position, nearby entities, and equipment are randomly injected to keep self-talk varied
- **Custom Prompt**: Customize the prompts used for self-talk / maid-to-maid chats for your own maids; supports both an all-maids setting and independent per-maid settings
- **Tool calls**: Off by default. When enabled, maids may switch states, fetch environment info, etc. on their own during self-talk / maid-to-maid chats. **Incurs extra token cost**
- **Per-player switch**: Each player can turn off self-talk for an individual maid; the host controls the master switch (on a dedicated server this requires editing the config file)
- **Trigger cooldown**: A configurable random interval range between triggers
- **Quiet while sleeping**: Allows maids to stay quiet while sleeping — self-talk and inter-chat won't trigger while they sleep
- **Dream talk** (**added in this fork**): A sleeping maid occasionally says one line of sleep-talk at a random interval (generated by the LLM from her persona), shown as a bubble and a chat message
- **Hard to wake** (**added in this fork**): When a player talks to a sleeping maid, the first few calls only get a drowsy mumble (no answer, no waking); after a random number of calls she wakes and replies normally
- **Visible in chat**: Self-talk and maid-to-maid chats are visible to nearby players (even if they are not the maid's owner)
- **Public API** (**added in this fork**): `SelfTalkApi.relayToMaid` lets another mod make one maid pass a sentence on to another maid on the server, and the latter answers through the inter-chat flow (see "API for other mods" below)

## Requirements

| Loader | Minecraft | Java | Touhou Little Maid |
| --- | --- | --- | --- |
| NeoForge | 1.21.1 | 21 | 1.5.3 |
| Forge | 1.20.1 | 17 | 1.5.3 |

## Installation

1. Install the mod: download the jar for your version
2. Launch the game and enable the master switch: Maid AI settings → Global AI settings → "Maid Self-Talk" → Enable; Maid Inter-Chat needs to be enabled separately in its own sub-page

## Usage

### Configuration

Maid AI chat settings → Global AI settings → "Maid Self-Talk":

- **State 1 / State 2**: enable switch, min/max trigger interval (seconds, typed input with range validation), player radius, and self-talk keep count for owner online / offline
- **Welcome**: the greeting trigger window after the owner logs in

Maid AI chat settings → Global AI settings → "Maid Inter-Chat":

- **Basics**: master switch (off by default), min/max trigger interval (seconds), player range (blocks), and maid range (blocks)
- **Context & chain**: keep rounds (a Q/A counts as one round; only the latest message remains when exceeded), chain probability (chance to continue after each reply), max chain length (hard cap on messages per session, guarding against maxed-out chain probability)

Maid AI chat settings → Global AI settings → "Maid Self-Talk" → "Sleep" (**added in this fork**):

- **Dream talk**: on/off, min/max interval (seconds), trigger radius (blocks; 0 means no player is required)
- **Hard to wake**: on/off, min/max calls to wake (inclusive), streak reset (seconds), stay-awake duration (seconds)

### Per-player settings

Open the maid's AI chat input screen (usually by pressing T) and click the 💬 button on the left to open the Maid Settings:

- **Self-talk / Inter-chat**: turn self-talk / inter-chat off individually
- **Sleep Quiet**: when on, self-talk / inter-chat no longer trigger while the maid is sleeping
- **Custom Prompt**: fill in separate prompts for all maids and for this maid
- **Tool**: global and per-maid switches; when enabled, this maid's self-talk / inter-chat can call tools

### Sleep: dream talk & hard to wake (added in this fork)

While a maid is fast asleep (she went to bed under a TLM night schedule) there are two new behaviours:

- **Dream talk**: At her own random interval (180–600 seconds by default) she says one line of sleep-talk,
  generated by the LLM from her persona and shown as a chat bubble and a chat message visible to nearby players.
  - Toggle `sleep.enabled` (on by default); interval `sleep.minIntervalSeconds` / `sleep.maxIntervalSeconds`
    (30–7200 seconds; swapped automatically if inverted).
  - Trigger radius `sleep.playerRange` (16 blocks by default): she only talks in her sleep when a player is within it
    (saves tokens). `0` means no player is required; the chat broadcast radius then falls back to 16 blocks.
  - **Relation to "Quiet while sleeping"**: that setting only blocks self-talk / inter-chat / greetings.
    Dream talk is controlled by its own switch — it still triggers when Quiet while sleeping is on.
    Dream talk still respects the master switch and the LLM prerequisites (LLM enabled, usable site, maid has a persona).
- **Hard to wake**: When a player talks to a sleeping maid, the first few calls do not get a normal reply —
  she only mumbles drowsily (she never answers what was said and does not wake). After a random number of calls N
  she wakes and replies normally, and that first reply carries a just-woken note
  (groggy / clingy / grumpy / gentle is decided by her own persona; no lines are hard-coded).
  - Toggle `sleep.wakeResistanceEnabled` (on by default).
  - `sleep.wakeMinCalls` / `sleep.wakeMaxCalls` (2 / 5 by default, range 1–20, both inclusive): when a streak starts,
    N is rolled in [min, max]; calls 1..N-1 only mumble, call N wakes her. `1` means she wakes on the first call.
  - `sleep.wakeStreakResetSeconds` (120 seconds by default): if more than that passes since the last call, the counter
    resets and the next call starts a new streak with a fresh N.
  - `sleep.wakeStayAwakeSeconds` (120 seconds by default, 0 disables): after being woken, she will not head back to
    bed within that window, so she does not drop straight back to sleep under a night schedule.
  - **Hard to wake intercepts the player chat entry point** (TLM's `MaidAIChatManager#chat`): on the mumble calls the
    request is cancelled, so the player's line is never sent to the LLM, consumes no tokens and is not written to the
    chat history; the player sees no "thinking" indicator and no error.
    Only calls from a real player who is the maid's owner or within 8 blocks are intercepted; everything else —
    including internal `chat()` calls from other mods — passes through.
- **Token cost**: each dream talk costs one LLM request; each mumble costs one LLM request
  (the waking call is a normal player chat request). When the LLM is unavailable (AI off, no usable site),
  dream talk does not trigger and mumbles stay silent (calls are still counted).

### Optional: AgentTweaks integration (Forge 1.20.1)

- AgentTweaks is not required: click 💬 in the AI chat screen to open Maid Settings, then close it to return to chat.
- This version provides a settings-navigation bridge. A Forge version of AgentTweaks that integrates with this bridge can provide unified navigation; closing Maid Settings returns to its parent screen.
- Custom Prompt edits are saved before switching pages; navigation is blocked while the environment-context overlay is open.
- No new network protocol or mandatory dependency is introduced. This provides the Self-Talk side only and does not imply that the Forge integration in AgentTweaks is complete.

### API for other mods (added in this fork)

A minimal public API for other add-on mods is available server-side: `com.maidmod.selftalk.api.SelfTalkApi` (static methods only, `API_VERSION = 1`, probe with `isAvailable()`).

```java
// Call on the server main thread: let `from` relay `text` to `to`, and have `to` reply via inter-chat
EntityMaid from = ...;
EntityMaid to = ...;
boolean accepted = SelfTalkApi.relayToMaid(from, to, "The master asked me to tell you dinner is ready.");
```

- **Threading**: must be called on the server main thread; calls from the client or network callback threads simply return `false` instead of crashing.
- **Return value**: `true` only means the request was accepted (queued or dispatched); it does not mean `to` has replied.
- **Rejected when**: any argument is `null`, both maids are the same, a maid is dead/removed, they are in different dimensions, or the text is blank; the Self-Talk master switch or the maid inter-chat switch is off; either maid's owner disabled inter-chat for her; either maid is already in an inter-chat chain/pair lock (an existing chain is never interrupted); `to` has no AI, the LLM master switch is off, the site is unavailable, or she has no persona.
- **Not checked**: this is an explicitly requested relay, so distance, cooldown and the global rate limit are not checked; continuation follows the existing chain-probability / max-chain-round config.
- **Visibility**: `to`'s reply is shown as a chat bubble and broadcast to nearby players as usual; `text` itself is not broadcast by this API.
- **Dependency advice**: use `compileOnly` plus an optional `mods.toml` dependency (`mandatory = false`), and call it reflectively from a dedicated bridge class (see how this repo handles AgentTweaks) so this mod does not become a hard dependency.

## License

[MIT](LICENSE)
