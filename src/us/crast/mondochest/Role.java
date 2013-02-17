package us.crast.mondochest;

import java.util.HashMap;
import java.util.Map;

public class Role {
    private static Map<String, Role> ROLES = new HashMap<String, Role>();
    private final String name;
    private boolean open_master_chest = false;
    private boolean open_slave_chest = false;
    private boolean manage_access = false;
    private boolean shelve = false;
    private boolean add_chests = false;
    private boolean remove_chests = false;

    public Role(String name) {
        this.name = name;
    }
    
    /** Chaining setters */
    public Role grantOpenMasterChest() {
        this.open_master_chest = true;
        return this;
    }
    
    public Role grantOpenSlaveChest() {
        this.open_slave_chest = true;
        return this;
    }
    
    public Role grantOpenAnyChest() {
        return this.grantOpenMasterChest().grantOpenSlaveChest();
    }
    
    public Role grantManageAccess() {
        this.manage_access = true;
        return this;
    }
    
    public Role grantShelve() {
        this.shelve = true;
        return this;
    }
    
    public Role grantAddChests() {
        this.add_chests = true;
        return this;
    }
    
    public Role grantRemoveChests() {
        this.remove_chests = true;
        return this;
    }
    
    
    /** Getters */
    
    public boolean canOpenMasterChest() {
        return this.open_master_chest;
    }
    
    public boolean canOpenSlaveChest() {
        return this.open_slave_chest;
    }
    
    public boolean canManageAccess() {
        return this.manage_access;
    }
    
    public boolean canShelve() {
        return this.shelve;
    }
    
    public boolean canAddChests() {
        return this.add_chests;
    }
    
    public boolean canRemoveChests() {
        return this.remove_chests;
    }
 
    public boolean canFind() {
        return this.open_slave_chest; // XXX no find role permission currently
    }

    public String getName() {
        return name;
    }
    
    /** Static methods */
    public static Role create(String name) {
        Role role = new Role(name);
        ROLES.put(name.toLowerCase(), role);
        return role;
    }
    
    public static Role find(String name) {
        return ROLES.get(name.toLowerCase());
    }

}
