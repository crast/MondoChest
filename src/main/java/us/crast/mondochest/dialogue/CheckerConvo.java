package us.crast.mondochest.dialogue;

import java.util.regex.Pattern;

import mondocommand.ChatMagic;

import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.RegexPrompt;
import org.bukkit.entity.Player;

import us.crast.chatmagic.BasicMessage;
import us.crast.chatmagic.MessageWithStatus;
import us.crast.chatmagic.MondoMessage;
import us.crast.chatmagic.Status;
import us.crast.mondochest.MondoChest;

public final class CheckerConvo implements ConversationAbandonedListener {
    private ConversationFactory conversationFactory;
    @SuppressWarnings("unused")
    private MondoChest plugin;

    public CheckerConvo(MondoChest plugin) {
        this.plugin = plugin;
        this.conversationFactory = new ConversationFactory(plugin)
            .withModality(false)
            .withPrefix(new ConvoPrefix())
            .withFirstPrompt(new CheckerPrompt())
            .withTimeout(60)
            .withEscapeSequence("/quit")
            .addConversationAbandonedListener(this);
    }

    public void begin(Player player) throws MondoMessage {
        conversationFactory.buildConversation(player).begin();
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent e) {
        if (!e.gracefulExit()) {
            e.getContext().getForWhom().sendRawMessage(BasicMessage.render(Status.ERROR, "Access conversation ended"));
        } else {
            e.getContext().getForWhom().sendRawMessage(BasicMessage.render(Status.INFO, "Bye!"));
        }
        
    }

    final class CheckerPrompt extends RegexPrompt {
        public CheckerPrompt() {
            super(Pattern.compile("^\\w+( .*)?"));
        }

        @Override
        public String getPromptText(ConversationContext ctx) {
            Object response = ctx.getSessionData("next_response");
            if (response != null && response instanceof MessageWithStatus) {
                ctx.setSessionData("next_response", null);
                return ((MessageWithStatus) response).render(false);
            } else {
                return ChatMagic.colorize("{USAGE}Commands: {AQUA}list{GOLD}, {AQUA}add{GOLD}, {AQUA}remove{GOLD}, {AQUA}quit");
            }
        }

        @SuppressWarnings("unused")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext ctx, String input) {
            String[] parts = input.split(" ", 3);
            MessageWithStatus response = null;
            String cmd = parts[0].toLowerCase();
            
            return this;
        }

    }

    public void shutdown() {
        conversationFactory = null;
    }
    
    public class ConvoPrefix implements ConversationPrefix {
        @Override
        public String getPrefix(ConversationContext arg0) {
            return ChatMagic.colorize("{GOLD}checker{RED}> {RESET}");
        }

    }
}
