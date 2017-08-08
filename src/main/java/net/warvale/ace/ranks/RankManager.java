package net.warvale.ace.ranks;

import net.warvale.ace.Main;
import net.warvale.ace.permissions.PermissionsManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class RankManager {
    private static Main plugin = Main.get();
    private static PermissionsManager pm = new PermissionsManager(plugin);
    //Returns the Prefix of the player's rank
    public static String getRankPrefix(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT prefix FROM ranks_locked WHERE id = "+getRankId(player)+" LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getString("prefix");
    }
    //Returns the Suffix of the player's rank
    public static String getRankSuffix(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT suffix FROM ranks_locked WHERE id = "+getRankId(player)+" LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getString("suffix");
    }
    //Returns the name color of the player's rank
    public static String getRankNameColor(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT color FROM ranks_locked WHERE id = "+getRankId(player)+" LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getString("color");
    }
    //Returns the id of the player's rank
    public static int getRankId(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT network_rank FROM users_locked WHERE uuid = '"+player.getUniqueId().toString()+"' LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getInt("network_rank");
    }
    //Returns the id of a rank with the name
    public static int getRankId(String rank) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT id FROM ranks_locked WHERE name = '"+rank+"' LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getInt("id");
    }
    //Returns the Name of the player's rank
    public static String getRankName(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT name FROM ranks_locked WHERE id = "+getRankId(player)+" LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getString("name");
    }
    //Creates a rank
    public static void createRank(String name) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("INSERT INTO `ranks_locked`(`name`, `prefix`, `suffix`, `color`) VALUES ('"+name+"',' ',' ','&f')");
        stmt.executeUpdate();
    }
    //Deletes a rank (Can not be undone)
    public static void deleteRank(String name) throws SQLException{
        for(Player p : getPlayersInRank(getRankId(name))){
            setPlayerRank(p, 0);
        }
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("DELETE FROM `ranks_locked` WHERE name='"+name+"'");
        stmt.executeUpdate();
    }
    public static void renameRank(String oldRank, String newRank) throws SQLException {
        int id = getRankId(oldRank);
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE ranks_locked SET name='"+newRank+"' WHERE id = "+id);
        stmt.executeUpdate();
    }
    //Sets the prefix of a specified rank
    public static void setPrefix(int id, String prefix) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE ranks_locked SET prefix='"+prefix+"' WHERE id = "+id);
        stmt.executeUpdate();
    }
    //Sets the suffix of a specified rank
    public static void setSuffix(int id, String suffix) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE ranks_locked SET suffix='"+suffix+"' WHERE id = "+id);
        stmt.executeUpdate();
    }
    //Sets the name color of a specified rank
    public static void setNameColor(int id, String colorCode) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE ranks_locked SET color='"+colorCode+"' WHERE id = "+id);
        stmt.executeUpdate();
    }
    //Set a specified player's rank
    public static void setPlayerRank(Player player, String rank) throws SQLException {
        int id = getRankId(rank);
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE users_locked SET network_rank="+id+" WHERE uuid = '"+player.getUniqueId().toString()+"'");
        stmt.executeUpdate();
        pm.setup(player);
    }
    //Set a specified player's rank
    public static void setPlayerRank(Player player, int id) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE users_locked SET network_rank="+id+" WHERE uuid = '"+player.getUniqueId().toString()+"'");
        stmt.executeUpdate();
        pm.setup(player);
    }
    //Returns an arraylist of all the ranks
    public static ArrayList<String> getAllRanks() throws SQLException{
        ArrayList<String> ranks = new ArrayList<>();
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT name FROM ranks_locked WHERE 1");
        ResultSet set = stmt.executeQuery();
        while(set.next()){
            ranks.add(set.getString("name"));
        }
        return ranks;
    }
    //Tests if a rank exists
    public static boolean doesRankExist(String name) throws SQLException {
        return getAllRanks().contains(name);
    }
    //Gets the permissions of a specified rank
    public static ArrayList<String> getPermissions(int id) throws SQLException{
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT permissions FROM ranks_locked WHERE id = "+id+" LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        String s = set.getString("permissions");
        String[] permissions = new String[(StringUtils.isEmpty(s)?1:StringUtils.length(s)+1)];
        if(StringUtils.isNotEmpty(s)){permissions = s.split("\\s+");}
        ArrayList<String> perms = new ArrayList<>();
        perms.addAll(Arrays.asList(permissions));
        return perms;
    }
    //Add a permission to a specified rank
    public static void addPermission(int id, String permission) throws SQLException {
        ArrayList<String> permissions = getPermissions(id);
        permissions.add(permission);
        StringBuilder newPermsSB = new StringBuilder();
        for (String s : permissions){
            newPermsSB.append(s);
            newPermsSB.append(" ");
        }
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE ranks_locked SET permissions='"+newPermsSB.toString()+"' WHERE id = "+id);
        stmt.executeUpdate();
        for(Player p : getPlayersInRank(id)){
            pm.setup(p);
        }
    }
    //Remove a permission from a specified rank
    public static void removePermission(int id, String permission) throws SQLException {
        ArrayList<String> permissions = getPermissions(id);
        permissions.remove(permission);
        StringBuilder newPermsSB = new StringBuilder();
        for (String s : permissions){
            newPermsSB.append(s);
            newPermsSB.append(" ");
        }
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE ranks_locked SET permissions='"+newPermsSB.toString()+"' WHERE id = "+id);
        stmt.executeUpdate();
        for(Player p : getPlayersInRank(id)){
            pm.setup(p);
        }
    }
    //Returns an ArrayList of the players in a rank
    public static ArrayList<Player> getPlayersInRank(int id)throws SQLException{
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT name FROM users_locked WHERE network_rank="+id);
        ResultSet set = stmt.executeQuery();
        set.next();
        ArrayList<Player> ps = new ArrayList<>();
        ArrayList<String> st = new ArrayList<>();
        for(String s : st){
            ps.add(Bukkit.getPlayer(s));
        }
        while(set.next()){
            st.add(set.getString("name"));
        }
        return ps;
    }

    //Wanted Level:

    public static void setWantedLevel(Player player, int level) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE users_locked SET locked_wanted="+level+" WHERE uuid = '"+player.getUniqueId()+"'");
        stmt.executeUpdate();
    }

    public static int getWantedLevel(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT locked_wanted FROM users_locked WHERE uuid = '"+player.getUniqueId()+"' LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getInt("locked_wanted");
    }

    public static String wantedLevelParser(int level){
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(level));
        sb.append("⋆");
        return sb.toString();
    }

    //Guard Level:
    //0=none, 1=guard, 2=warden
    public static void setGuardLevel(Player player, int level) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("UPDATE users_locked SET locked_guard="+level+" WHERE uuid = '"+player.getUniqueId()+"'");
        stmt.executeUpdate();
    }

    public static int getGuardLevel(Player player) throws SQLException {
        PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT locked_guard FROM users_locked WHERE uuid = '"+player.getUniqueId()+"' LIMIT 1");
        ResultSet set = stmt.executeQuery();
        set.next();
        return set.getInt("locked_guard");
    }

    public static String guardLevelParser(int level){
        String s;
        switch (level){
            case 1:
                s="✵";
                break;
            case 2:
                s="❈";
                break;
            default:
                s="";
                break;
        }
        return s;
    }
}