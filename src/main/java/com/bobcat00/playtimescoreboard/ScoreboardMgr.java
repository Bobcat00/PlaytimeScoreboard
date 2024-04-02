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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;

public class ScoreboardMgr
{
    private PlaytimeScoreboard plugin;
    private ScoreboardManager manager;
    private Scoreboard scoreboard;
    private Objective obj;
    
    public ScoreboardMgr(PlaytimeScoreboard plugin)
    {
        this.plugin = plugin;
        
        manager = Bukkit.getServer().getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        obj = scoreboard.registerNewObjective("PlaytimeScoreboard", Criteria.DUMMY, "Playtime (minutes)");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    
    // -------------------------------------------------------------------------
    
    public void update(final Map<String, Integer> scoreMap)
    {
        // Get names of all online players
        
        List<String> onlinePlayers = new ArrayList<String>();
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            onlinePlayers.add(player.getName());
        }
        
        // Clear old scores
        
        for (String entry : scoreboard.getEntries())
        {
            scoreboard.resetScores(entry);
        }
        
        // Update all entries
        
        for (Map.Entry<String,Integer> entry : scoreMap.entrySet())
        {
            if (onlinePlayers.contains(entry.getKey()))
            {
                obj.getScore(entry.getKey()).setScore(entry.getValue());
            }
            else
            {
                obj.getScore(ChatColor.GRAY + entry.getKey()).setScore(entry.getValue());
            }
        }
        
        // Send to all online players
        
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            player.setScoreboard(scoreboard);
        }
    }
}
