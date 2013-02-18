package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.BlockState;

public enum MondoSign {
    MASTER ("[MondoChest]", org.bukkit.block.Chest.class),
    SLAVE ("[MondoSlave]", org.bukkit.block.Chest.class),
    RELOAD ("[MondoReload]", org.bukkit.block.Dispenser.class),
    NO_SIGN("", null);
    
    private final String signText;
    private final Class<? extends BlockState> matchClass;
    private static Map<String, MondoSign> signMap = new HashMap<String, MondoSign>();
    private static Map<String, MondoSign> lowercaseSignMap = new HashMap<String, MondoSign>();
    
    MondoSign(String signText, Class<? extends BlockState> c) {
        this.signText = signText;
        matchClass = c;
    }
    
    public String getSignText() {
        return signText;
    }
    
    public Class<? extends BlockState> getMatchClass() {
        return matchClass;
    }
    
    public static MondoSign match(String signText) {
        MondoSign sign = null;
        if (MondoConfig.CASE_INSENSITIVE_SIGNS) {
            sign = lowercaseSignMap.get(signText.toLowerCase());
        } else {
            sign = signMap.get(signText);
        }
        if (sign == null) sign = NO_SIGN;
        return sign;
    }
    
    static {
        for (MondoSign s : values()) {
            signMap.put(s.getSignText(), s);
            lowercaseSignMap.put(s.getSignText().toLowerCase(), s);
        }
    }
}
