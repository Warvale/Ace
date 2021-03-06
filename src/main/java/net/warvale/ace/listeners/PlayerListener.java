package net.warvale.ace.listeners;

import net.warvale.ace.Main;
import net.warvale.ace.permissions.PermissionsManager;
import net.warvale.ace.ranks.RankManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerListener implements Listener {
    private Main plugin;
    private PermissionsManager pm;

    public PlayerListener(Main plugin, PermissionsManager pm){
        this.plugin = plugin;
        this.pm = pm;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        //Put player into database
        try {
            PreparedStatement stmt = plugin.getDb().getConnection().prepareStatement("SELECT * FROM users WHERE uuid = '"+player.getUniqueId().toString()+"' LIMIT 1");
            ResultSet set = stmt.executeQuery();
            if (!set.next()) {
                stmt.close();
                stmt = plugin.getDb().getConnection().prepareStatement("INSERT INTO users (uuid, name) VALUES ('"+player.getUniqueId().toString()+"', '"+player.getName()+"')");
                stmt.execute();
                stmt.close();
            }
            set.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        pm.setup(player);
        try {
            String prfx = RankManager.getRankPrefix(player);
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',prfx+" "+RankManager.getRankNameColor(player))+player.getName());
        }catch(Exception err) {err.printStackTrace();}

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        pm.remove(player);
    }
}
