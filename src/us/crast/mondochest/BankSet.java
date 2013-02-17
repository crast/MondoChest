package us.crast.mondochest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import us.crast.datastructures.DefaultDict;
import us.crast.mondochest.util.ChestManagerSet;
import us.crast.utils.DecodeResults;
import us.crast.utils.GenericUtil;
import us.crast.utils.StringTools;

@SerializableAs("MondoChestSet")
public final class BankSet implements ConfigurationSerializable {
    private String world;
    private String owner;
    private BlockVector masterSign;
    private ChestManager masterChest;
    private Set<ChestManager> chestLocations = new java.util.HashSet<ChestManager>();
    private DefaultDict<Material, ChestManagerSet> materialChests = new DefaultDict<Material, ChestManagerSet>(
            ChestManagerSet.getMaker());
    private DefaultDict<MaterialWithData, ChestManagerSet> materialDataChests = new DefaultDict<MaterialWithData, ChestManagerSet>(
            ChestManagerSet.getMaker());
    private Map<String, Role> acl = null;

    public BankSet(Chest masterChest, String owner, BlockVector masterSign) {
        this.owner = owner;
        this.masterChest = new ChestManager(masterChest, false);
        this.masterSign = masterSign;
        this.setWorld(masterChest.getWorld().getName());
    }

    private BankSet(ChestManager masterChest, String owner,
            BlockVector masterSign) {
        this.owner = owner;
        this.masterChest = masterChest;
        this.masterSign = masterSign;
    }

    public void restackSpecial(World world) {
        masterChest.restackSpecial(world);
    }

    public boolean add(BlockState block, boolean allow_restack) {
        if (block.getType() == Material.CHEST) {
            return addChest((Chest) block, allow_restack);
        } else if (block.getType() == Material.DISPENSER) {
            return addDispenser((Dispenser) block, allow_restack);
        } else {
            return false;
        }
    }

    public boolean addDispenser(Dispenser block, boolean allow_restack) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addChest(Chest chest) {
        return addChest(chest, false);
    }

    public boolean addChest(Chest chest, boolean allow_restack) {
        ChestManager newmanager = new ChestManager(chest, allow_restack);
        if (newmanager.equals(masterChest))
            return false;
        if (chestLocations.contains(newmanager))
            return false;
        clearDuplicates(newmanager);
        chestLocations.add(newmanager);
        return true;
    }

    private void clearDuplicates(ChestManager newmanager) {
        // Deal with a specialty case if we add a double chest where there was
        // previously a single, or vice versa.
        if (newmanager.getChest2() != null) {
            for (BlockVector vec : newmanager.internalBlockLocations()) {
                ChestManager test = new ChestManager(vec, null, false);
                chestLocations.remove(test);
            }
        } else {
            BlockVector candidate = newmanager.getChest1();
            for (ChestManager cm : chestLocations) {
                if (candidate.equals(cm.getChest1())
                        || candidate.equals(cm.getChest2())) {
                    chestLocations.remove(cm);
                    break;
                }
            }
        }

    }

    public boolean removeChest(Chest chest) {
        ChestManager other = new ChestManager(chest, false);
        return chestLocations.remove(other);
    }

    private void addChestManager(ChestManager manager) {
        // XXX de-serializing from version 0.5 might allow a ChestManager to be
        // added which is equal to the master, so stop it by ignoring its add.
        if (manager.equals(masterChest)) {
            MondoConfig
                    .logDecodeError("Found a slave chest with the same coordinates as a master. "
                            + "Removing it to prevent any future issues.");
            return;
        }
        chestLocations.add(manager);
    }

    public java.util.Set<Material> refreshMaterials(World world) {
        materialChests.clear();
        materialDataChests.clear();
        for (ChestManager m : chestLocations) {
            for (ItemStack stack : m.listItems(world)) {
                materialDataChests.ensure(new MaterialWithData(stack)).add(m);
                materialChests.ensure(stack.getType()).add(m);
            }
        }
        return materialChests.keySet();
    }

    public int shelveItems(World world) {
        int num_shelved = 0;
        Set<ChestManager> to_restack = new java.util.HashSet<ChestManager>();
        for (ItemStack stack : masterChest.listItems(world)) {
            // log.info(String.format("Item: %d of %s", stack.getAmount(),
            // stack.getType().toString()));
            MaterialWithData md = new MaterialWithData(stack);
            ChestManagerSet destinations = materialDataChests.get(md);
            if (destinations == null) {
                Material m = stack.getType();
                destinations = materialChests.get(m);
            }
            if (destinations != null) {
                for (ChestManager dest : destinations) {
                    // log.info("Stack of " + stack.getType().toString() +
                    // " starting quantity: " + stack.getAmount());
                    HashMap<Integer, ItemStack> failures = dest.addItem(world,
                            stack);
                    if (failures.isEmpty() && stack.getAmount() >= 0) {
                        masterChest.removeItem(world, stack);
                        if (dest.isRestackAllowed()) {
                            to_restack.add(dest);
                        }
                        num_shelved++;
                        break;
                    } else {
                        // ChestManager.printWeirdStack(failures);
                    }
                    // log.info("Stack of " + stack.getType().toString() +
                    // " ending quantity: " + stack.getAmount());
                }
            }
        }
        if (MondoConfig.RESTACK_SLAVES) {
            for (ChestManager chest : to_restack) {
                chest.restackSpecial(world);
            }
        }
        return num_shelved;
    }

    public int numChests() {
        return chestLocations.size();
    }

    public Collection<ChestManager> allChestManagers() {
        ArrayList<ChestManager> managers = new ArrayList<ChestManager>(
                chestLocations);
        managers.add(masterChest);
        return managers;
    }

    /* Getters/setters */

    public Collection<ChestManager> listSlaves() {
        return chestLocations;
    }

    public ChestManager getMasterChest() {
        return masterChest;
    }

    public BlockVector getMasterSign() {
        return masterSign;
    }

    public String getKey() {
        return StringTools.md5String(this.masterSign.toString());
    }

    public String getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String toString() {
        return String.format("<Bankset: master(%s), owner=%s, %d chests>",
                masterChest, getOwner(), numChests());
    }

    /* Serialization */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> d = new HashMap<String, Object>();
        d.put("masterChest", masterChest);
        d.put("masterSign", masterSign);
        d.put("chestLocations", new ArrayList<ChestManager>(chestLocations));
        d.put("owner", owner);
        if (acl != null && !acl.isEmpty()) {
            d.put("acl", stringAcl());
        }
        return d;
    }

    public Map<String, String> stringAcl() {
        Map<String, String> sAcl = new HashMap<String, String>();
        for (Map.Entry<String, Role> e : acl.entrySet()) {
            sAcl.put(e.getKey(), e.getValue().getName());
        }
        return sAcl;
    }

    public static BankSet deserialize(Map<String, Object> d) {
        BankSet bankset = new BankSet((ChestManager) d.get("masterChest"),
                (String) d.get("owner"), (BlockVector) d.get("masterSign"));

        Object locations = d.get("chestLocations");
        if (locations instanceof Collection<?>) {
            DecodeResults<ChestManager> results = GenericUtil.decodeCollection(
                    locations, ChestManager.class);
            for (ChestManager location : results.validValues) {
                bankset.addChestManager(location);
            }
            for (Object badValue : results.failedValues) {
                MondoConfig
                        .logDecodeError(String
                                .format("when building chestLocations for bankset %s, expected a ChestManager, got a %s",
                                        bankset.toString(), badValue.getClass()
                                                .getName()));
            }
        } else if (locations == null) {
            MondoConfig.logDecodeError("chestLocations appears to be null");
        } else {
            MondoConfig
                    .logDecodeError("chestLocations is supposed to be a list, wtf mait, got: "
                            + locations.getClass().getName());
        }
        Object acl = d.get("acl");
        if (acl != null) {
            if (acl instanceof Collection<?>) {
                MondoConfig.getLog().warning("Handling ACL as a collection");
                DecodeResults<String> results = GenericUtil.decodeCollection(
                        acl, String.class);
                for (String user : results.validValues) {
                    if (!bankset.addAccess(user, "user"))
                        MondoConfig.logDecodeError("Invalid role???");
                }
                if (results.hasFailures()) {
                    MondoConfig
                            .logDecodeError("ACL entries should be strings.");
                }
            } else if (acl instanceof Map<?, ?>) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) acl).entrySet()) {
                    bankset.addAccess((String) entry.getKey(),
                            (String) entry.getValue());
                }
            } else {
                MondoConfig
                        .logDecodeError("ACL is supposed to be a collection");
            }
        }

        return bankset;
    }

    public boolean addAccess(String name, String role) {
        Role mrole = Role.find(role);
        if (mrole == null)
            return false;
        getAcl().put(name, mrole);
        return true;
    }

    public boolean removeAccess(String name) {
        return getAcl().remove(name) != null;
    }

    public Role getAccess(String name) {
        Role role = getAcl().get(name);
        if (role == null)
            role = Role.find(MondoConfig.FALLBACK_ROLE);
        return role;
    }

    public Role getAccess(Player player) {
        return getAccess(player.getName());
    }

    public Map<String, Role> getAcl() {
        if (acl == null)
            acl = new HashMap<String, Role>();
        return acl;
    }

}