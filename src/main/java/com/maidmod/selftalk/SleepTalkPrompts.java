package com.maidmod.selftalk;

/**
 * 睡眠相关的三段提示词（梦话 / 被叫时迷糊嘟囔 / 被叫醒后的反应），中英各一套。
 * <p>
 * 调用方按女仆当前对话语言是否中文（{@code SelfTalkContexts.isChinese}）选择 {@code zh}；
 * 语言既不是中文也不是英文时用英文版，并照现有惯例在末尾追加语言指令
 * （{@code SelfTalkContexts.languageInstruction}）。
 * <p>
 * 提示词集中在这里统一维护，业务代码只通过这些方法取文字，不要在别处另拼。
 */
public final class SleepTalkPrompts {
    private SleepTalkPrompts() {
    }

    /** 女仆熟睡时自发说梦话（呓语）的指令：只输出一句梦话。 */
    public static String dreamTalk(boolean zh) {
        return zh ? """
                你现在正在熟睡，说的是梦话。
                请结合你的人设，说一句简短的梦话或含糊的呓语（不超过 20 个字）：可以是梦见的事、白天放不下的牵挂、对主人的嘟囔，也可以只是"嗯……""呼……"之类含糊的声音。
                要求：
                - 你是在说梦话，没有醒来，也不是在和谁对话；不要提到自己在做梦或在说梦话。
                - 只输出这一句梦话本身，不要加引号、动作描写或任何解释。
                """.trim() : """
                You are fast asleep and talking in your sleep.
                Based on your persona, say one short line of sleep-talk or a muddled murmur (no more than 20 words): something you are dreaming about, a worry from the day, a mumble about your owner, or just a vague sound like "mm..." or "zzz...".
                Requirements:
                - You are talking in your sleep: you have not woken up and you are not talking to anyone; do not mention dreaming or sleep-talking.
                - Output only that one line itself, without quotation marks, action descriptions or any explanation.
                """.trim();
    }

    /** 玩家在叫熟睡的女仆、但她还没被叫醒时：只用一两句含混的话回应，不回答玩家的内容。 */
    public static String mumbleReply(boolean zh) {
        return zh ? """
                你正在熟睡，玩家刚才在叫你，但你没有被叫醒。
                请只用一两句含混的梦话或迷迷糊糊的嘟囔回应（不超过 20 个字）：不要真正回答玩家说的内容，也不要醒来；可以把玩家的声音混进梦里，也可以只是翻个身哼哼两声。保持符合你的人设。
                只输出这一两句话本身，不要加引号、动作描写或任何解释。
                """.trim() : """
                You are fast asleep. The player just called you, but you have not woken up.
                Reply with only one or two muddled sleep-talk lines or a drowsy mumble (no more than 20 words): do not really answer what the player said and do not wake up; you may blend the player's voice into your dream, or just turn over and hum. Stay true to your persona.
                Output only those one or two lines themselves, without quotation marks, action descriptions or any explanation.
                """.trim();
    }

    /**
     * 熟睡一会儿之后，请她编出这一觉做的梦。回复只存起来供梦话与醒来后引用，不显示给任何人。
     * 消息前缀里已经带着她和玩家之间的对话历史，所以直接让她从"上面发生过的事"里取材。
     */
    public static String dreamGenerate(boolean zh) {
        return zh ? """
                你已经睡着了，正在做梦。
                请结合你的人设，以及上面你和玩家之间发生过的事（说过的话、一起做的事、你放不下的牵挂），编出你这一觉做的梦：
                - 要有梦的味道：场景可以跳来跳去、有点荒诞，把白天的人和事混在一起，但要能看出是由哪些事变来的。
                - 是甜的、好笑的、有点不安的还是让人害羞的，由你的人设决定；不要太长。
                - 用第一人称写，两三句话，总共不超过 80 个字。
                - 只输出梦的内容本身，不要加引号、标题、动作描写，也不要解释这是你编的。
                """.trim() : """
                You have fallen asleep and are dreaming.
                Based on your persona and what has happened between you and the player above (what was said, what you did together, what is weighing on your mind), make up the dream you are having:
                - It should feel like a dream: scenes may jump around and be a little absurd, mixing the people and events of the day, yet you can still tell what they were made from.
                - Whether it is sweet, funny, a bit uneasy or embarrassing is up to your persona; keep it short.
                - Write it in the first person, two or three sentences, no more than 60 words in total.
                - Output only the dream itself, without quotation marks, a title or action descriptions, and do not explain that you made it up.
                """.trim();
    }

    /**
     * 做了梦之后说的梦话：同样只说一句，但取材于她这一觉的梦。{@code dream} 是她自己编的梦，只有她自己知道，
     * 要她说梦里的一个片段而不是照抄。
     */
    public static String dreamTalkAboutDream(boolean zh, String dream) {
        return zh ? """
                你现在正在熟睡，说的是梦话。你此刻正做着这样一个梦（只有你自己知道）：
                %s

                请从这个梦里取一个片段，说一句简短的梦话（不超过 20 个字）：可以是梦里的一句台词、对梦里某个人或某件事的反应，也可以是含糊的呓语。
                要求：
                - 你是在说梦话，没有醒来，也不是在和谁对话；不要提到自己在做梦或在说梦话，也不要把整个梦复述一遍。
                - 只输出这一句梦话本身，不要加引号、动作描写或任何解释。
                """.formatted(dream).trim() : """
                You are fast asleep and talking in your sleep. You are dreaming this dream right now (only you know it):
                %s

                Take one fragment of that dream and say one short line of sleep-talk (no more than 20 words): a line spoken in the dream, a reaction to someone or something in it, or a muddled murmur.
                Requirements:
                - You are talking in your sleep: you have not woken up and you are not talking to anyone; do not mention dreaming or sleep-talking, and do not retell the whole dream.
                - Output only that one line itself, without quotation marks, action descriptions or any explanation.
                """.formatted(dream).trim();
    }

    /**
     * 自己睡醒之后（没有人叫她），把梦讲给主人。{@code dream} 是她这一觉的梦。
     */
    public static String dreamReport(boolean zh, String dream) {
        return zh ? """
                你刚刚自己睡醒了（没有人叫你）。你想起了睡着时做的梦，梦的内容是：
                %s

                请按你的人设，用一两句话（不超过 50 个字）把这个梦告诉玩家：可以带着刚睡醒的迷糊，可以害羞、开心，或者有点在意；不必把梦原样复述，挑最让你在意的那一点说，语气像是跟身边的人闲聊。
                只输出你要说的话本身，不要加引号、动作描写或任何解释，也不要提到这条提示。
                """.formatted(dream).trim() : """
                You have just woken up on your own (nobody called you). You remember the dream you had while asleep:
                %s

                Based on your persona, tell the player about this dream in one or two sentences (no more than 40 words): you may be groggy from just waking, shy, happy or a little bothered; you need not retell it as it was, just pick the one thing that stays with you most, in the tone of a chat with someone beside you.
                Output only what you say, without quotation marks, action descriptions or any explanation, and do not mention this note.
                """.formatted(dream).trim();
    }

    /**
     * 被叫醒时，附在起床提示后面的一句：她被叫醒前正做着这个梦，合适的话可以顺口提一句。
     */
    public static String dreamWakeHint(boolean zh, String dream) {
        return zh
                ? "（你被叫醒之前正做着这样一个梦：" + dream + " 如果合适，可以顺口提一句，但不要因此忽略玩家说的话，也不要提到这条提示。）"
                : "(Just before you were woken you were having this dream: " + dream
                + " If it fits, you may mention it in passing, but do not ignore what the player said, and do not mention this note.)";
    }

    /** 被反复叫醒后的第一次正常回复前，附在玩家这句话上的提示，让她按性格自然地"起床"。 */
    public static String wakeUpNote(boolean zh) {
        return zh
                ? "（你刚才在熟睡，被玩家反复叫了好几次才被吵醒。请按你的性格自然地反应：迷糊、撒娇、有起床气，还是温柔，都由你的人设决定；先用一两句话表现出刚醒来的状态，再回应玩家说的话。回应时不要提到这条提示。）"
                : "(You were fast asleep and were only woken after the player called you several times. React naturally according to your personality: groggy, clingy, grumpy or gentle is up to your persona; first show in a sentence or two that you have just woken up, then respond to what the player said. Do not mention this note in your reply.)";
    }
}
