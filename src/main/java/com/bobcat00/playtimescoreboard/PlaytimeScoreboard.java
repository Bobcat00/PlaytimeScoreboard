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
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaytimeScoreboard extends JavaPlugin
{
    Playtime playtime;
    
    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        
        playtime = new Playtime(this);
    }
    
    @Override
    public void onDisable()
    {
        // HandlerList.unregisterAll(listeners);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
        {
            // Reload config
            reloadConfig();
            // Recreate player map
            playtime.initPlayerMap();
            // Update scoreboard
            playtime.update();
            
            sender.sendMessage("PlaytimeScoreboard reloaded.");
            return true; // Normal return
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
    {
        List<String> argList = new ArrayList<>();
        if (args.length == 1)
        {
            argList.add("reload");
            return argList.stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
        }
        return argList; // returns an empty list
    }
}
