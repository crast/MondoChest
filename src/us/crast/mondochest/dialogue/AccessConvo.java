package us.crast.mondochest.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.RegexPrompt;
import org.bukkit.entity.Player;

import us.crast.mondochest.BankSet;
import us.crast.mondochest.MessageWithStatus;
import us.crast.mondochest.MondoChest;
import us.crast.mondochest.MondoConfig;
import us.crast.mondochest.MondoConstants;
import us.crast.mondochest.MondoListener;
import us.crast.mondochest.MondoMessage;
import us.crast.mondochest.Status;
import us.crast.mondochest.command.BasicMessage;
import us.crast.mondochest.persist.BankManager;
import us.crast.mondochest.util.ChatMagic;
import us.crast.utils.CollectionUtil;

public final class AccessConvo implements ConversationAbandonedListener {
    private ConversationFactory conversationFactory;
    private MondoListener listener;
    private BankManager bankManager;

    public AccessConvo(MondoChest plugin, MondoListener listener) {
        this.bankManager = plugin.getBankManager();
        this.listener = listener;
        this.conversationFactory = new ConversationFactory(plugin)
            .withModality(false)
            .withPrefix(new ConvoPrefix())
            .withFirstPrompt(new AccessPrompt())
            .withTimeout(60)
            .withEscapeSequence("/quit")
            .addConversationAbandonedListener(this);
    }

    public void begin(Player p) {
        conversationFactory.buildConversation(p).begin();
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent e) {
        if (!e.gracefulExit()) {
            e.getContext().getForWhom().sendRawMessage(BasicMessage.render(Status.ERROR, "Conversation left"));
        } else {
            e.getContext().getForWhom().sendRawMessage(BasicMessage.render(Status.INFO, "Bye!"));
        }
        
    }

    public MessageWithStatus listAccess(Player player, int page) throws MondoMessage {
        if (!MondoConfig.ACL_ENABLED) {
            throw new MondoMessage(MondoConstants.ACL_ENABLED_MESSAGE, Status.ERROR);
        }
        BankSet bank = listener.getLastClickedBank(player, true);
        if (bank.getAcl().isEmpty()) {
            return new BasicMessage("Allowed Users: EVERYONE", Status.SUCCESS);
        } else {
            List<String> users = new ArrayList<String>();
            for (Map.Entry<String, String> entry : CollectionUtil.sortedDictEntries(bank.stringAcl())) {
                users.add(ChatMagic.colorize("{LIGHT_PURPLE}%s {RED}(%s){GOLD}", entry.getKey(), entry.getValue()));
            }
            String allowed = StringUtils.join(users, ", ");
            return new BasicMessage(
                ChatMagic.colorize("Allowed Users: {LIGHT_PURPLE}%s", allowed),
                Status.SUCCESS
            );
        }
    }
    
    public MessageWithStatus addAccess(Player player, String target, String role) throws MondoMessage {
        BankSet bank = listener.getLastClickedBank(player, true);
        if (bank.addAccess(target, role)) {
            bankManager.markChanged(bank, true);
            return new BasicMessage(Status.SUCCESS, "Added user %s", target);
        } else {
            return new BasicMessage(Status.ERROR, "Didn't add user");
        }
    }
    
    public MessageWithStatus removeAccess(Player player, String target) throws MondoMessage {
        BankSet bank = listener.getLastClickedBank(player, true);
        if (bank.removeAccess(target)) {
            bankManager.markChanged(bank, true);
            return new BasicMessage(Status.SUCCESS, "Removed user %s", target);
        }else {
            return new BasicMessage(Status.ERROR, "WOT");
        }
    }

    final class AccessPrompt extends RegexPrompt {
        public AccessPrompt() {
            super(Pattern.compile("^\\w+( .*)?"));
        }

        @Override
        public String getPromptText(ConversationContext ctx) {
            Object response = ctx.getSessionData("next_response");
            if (response != null && response instanceof MessageWithStatus) {
                ctx.setSessionData("next_response", null);
                return BasicMessage.render((MessageWithStatus) response, false);
            } else {
                return ChatMagic.colorize("{USAGE}Commands: {AQUA}list{GOLD}, {AQUA}add{GOLD}, {AQUA}remove{GOLD}, {AQUA}quit");
            }
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext ctx, String input) {
            String[] parts = input.split(" ", 3);
            Player player = (Player) ctx.getForWhom();
            MessageWithStatus response = null;
            String cmd = parts[0].toLowerCase();
            try {
                if (cmd.equals("list")) {
                    int page = (parts.length == 2)? Integer.parseInt(parts[1]) : 1;
                    response = listAccess(player, page);
                } else if (cmd.equals("add")) {
                    if (parts.length >= 2) {
                        String role = (parts.length == 3)? parts[2] : "user";
                        response = addAccess(player, parts[1], role);
                    } else {
                        response = new BasicMessage("Usage: add <name> [<role>]", Status.USAGE);
                    }
                } else if (cmd.equals("remove")) {
                    if (parts.length == 2) {
                        response = removeAccess(player, parts[1]);
                    } else {
                        response = new BasicMessage("Usage: remove <name>", Status.USAGE);
                    }
                } else if (cmd.equals("quit")) {
                    return null;
                }
            } catch (MondoMessage m) {
                response = m;
            }
            if (response != null) {
                ctx.setSessionData("next_response", response);
            }
            return this;
        }

    }

    public void shutdown() {
        listener = null;
        conversationFactory = null;
    }
    
    public class ConvoPrefix implements ConversationPrefix {
        @Override
        public String getPrefix(ConversationContext arg0) {
            return ChatMagic.colorize("{GOLD}access{RED}> {RESET}");
        }

    }
}
