// PlaytimeScoreboard - Show playtime on the scoreboard
// Copyright 2024 Bobcat00
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.bobcat00.playtimescoreboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitRunnable;

public final class Playtime extends BukkitRunnable implements Listener
{
    final private int maxEntries = 15;
    private PlaytimeScoreboard plugin;
    private Map<String, Integer> playerMap = new HashMap<String, Integer>();
    private ScoreboardMgr mgr;
    
    public Playtime(PlaytimeScoreboard plugin)
    {
        this.plugin = plugin;
        
        // Get all players who have ever visited and save their names and playtimes
        
        final OfflinePlayer[] players = Bukkit.getServer().getOfflinePlayers();
        for (OfflinePlayer player : players)
        {
            // Exclude banned players
            if (!player.isBanned())
            {
                String name = player.getName();
                int time = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (60*20);
                playerMap.put(name, time);
            }
        }
        
        // Create scoreboard
        
        mgr = new ScoreboardMgr(plugin);
        
        // Register listeners
        
        plugin.getServer().getPluginManager().registerEvent(PlayerJoinEvent.class, this, EventPriority.MONITOR,
                new EventExecutor() { public void execute(Listener l, Event e) { onPlayerJoin((PlayerJoinEvent)e); }},
                plugin);
        
        plugin.getServer().getPluginManager().registerEvent(PlayerQuitEvent.class, this, EventPriority.MONITOR,
                new EventExecutor() { public void execute(Listener l, Event e) { onPlayerQuit((PlayerQuitEvent)e); }},
                plugin);
        
        // Start periodic task
        
        this.runTaskTimer(plugin,   // plugin
                          1L,       // delay
                          60L*20L); // period
    }
    
    // -------------------------------------------------------------------------
    
    // Periodic task to run updates
    
    @Override
    public void run()
    {
        update();
    }
    
    // -------------------------------------------------------------------------
    
    // Update playtimes of online players and update scoreboard
    
    private void update()
    {
        // Update map for online players
        
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            String name = player.getName();
            int time = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (60*20);
            playerMap.put(name, time);
        }
        
        // Sort list and get top values
        
        Comparator<Entry<String, Integer>> comp = Entry
                .<String, Integer>comparingByValue().reversed()
                .thenComparing(Entry.comparingByKey());

        final Map<String, Integer> sortedMap = playerMap.entrySet().stream().sorted(comp).
                                                         collect(Collectors.toMap(Entry::getKey,
                                                                                  Entry::getValue,
                                                                                  (a, b) -> a, // merge function
                                                                                  LinkedHashMap::new));
        
        // Exclude list from config
        final List<String> excludeList = plugin.getConfig().getStringList("exclude");
        
        // Short map
        Map<String, Integer> shortMap = new LinkedHashMap<String, Integer>();
        
        Iterator<Entry<String, Integer>> it = sortedMap.entrySet().iterator();
        int i = 0;
        while (it.hasNext() && i < maxEntries)
        {
            Entry<String, Integer> entry = it.next();
            if (!excludeList.contains(entry.getKey()))
            {
                shortMap.put(entry.getKey(), entry.getValue());
                ++i;
            }
        }
        
        // Update scoreboard and send to players
        
        mgr.update(shortMap);
    }
    
    // -------------------------------------------------------------------------
    
    // Send scoreboard to player
    
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        // Delay by 150 msec (3 ticks) to allow getOnlinePlayers to update
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                update();
            }
        }, 3L); // time delay (ticks)
    }
    
    // -------------------------------------------------------------------------
    
    // Save updated time in map
    
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();
        String name = player.getName();
        int time = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (60*20);
        playerMap.put(name, time);

        // Delay by 150 msec (3 ticks) to allow getOnlinePlayers to update
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                update();
            }
        }, 3L); // time delay (ticks)
    }
}
