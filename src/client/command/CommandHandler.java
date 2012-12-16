package client.command;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.*;
import net.server.Channel;
import net.server.Server;
import net.server.World;
import net.server.handlers.channel.ChangeChannelHandler;
import server.maps.MapleMap;
import tools.StringUtil;

/**
 * I don't suggest modifying this class unless you know what your doing.
 * If you know what your doing please let me know how you improved it. :)
 * @author Tagette aka Handsfree
 */
public class CommandHandler {

    /**
     * Used by the command executer to tell if the method is a command or not.
     */
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Command {
        boolean value() default true;
    }

    /**
     * Used when creating help for the command handler.
     */
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Syntax {

        String value() default "NULL";
    }

    /**
     * Used when creating help for the command handler.
     */
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Description {

        String value() default "No description.";
    }

    /**
     * Used when executing a command that has aliases.
     */
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Alias {

        String[] value();
    }

    /**
     * Used for when commands need to be differentiated.
     */
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Tags {

        String[] value();
    }

    public class CommandInfo {

        public String name;
        public String syntax;
        public String description;
        public String[] alias;
        public String[] tags;

        public CommandInfo() {
            name = "";
            syntax = "";
            description = "";
            alias = null;
            tags = null;
        }

        @Override
        public String toString() {
            return syntax + " - " + description;
        }
        
    }
    
    public class CmdSortASC implements Comparator<CommandInfo> {

        @Override
        public int compare(CommandInfo t, CommandInfo t1) {
            return t.name.compareTo(t1.name);
        }
        
    }
    
    public class CmdSortDSC implements Comparator<CommandInfo> {

        @Override
        public int compare(CommandInfo t, CommandInfo t1) {
            return t1.name.compareTo(t.name);
        }
        
    }
    
    protected char header;
    protected MapleClient client;
    protected MapleCharacter chr;
    protected Server server;
    protected World world;
    protected Channel channel;
    protected String label;
    protected String sub;
    protected String[] args;
    protected String command;
    private boolean handled;
    
    private static Map<String, Method> cachedCommands = new HashMap<String, Method>();

    public CommandHandler(MapleClient client, char header, String[] args) {
        this.client = client;
        this.chr = client.getPlayer();

        this.server = Server.getInstance();
        this.world = client.getWorldServer();
        this.channel = client.getChannelServer();

        this.header = header;
        this.args = args;
        this.label = args[0].toLowerCase();
        if(args.length >= 2)
            this.sub = args[1].toLowerCase();
        else
            this.sub = "";
        this.command = this.header + StringUtil.joinStringFrom(args, 0, " ");
        
        this.handled = false;
    }

    /**
     * Executes a command and checks for packet spamming.
     * 
     * @return Returns false when the player isn't high enough gm level.
     */
    public void execute() throws Exception {
        // Packet Spam
        // It will not run the command if there isn't enough time between commands.
        if (System.currentTimeMillis() - chr.getLastUse() < 200) {
            chr.updateLastUse();
            return;
        }
        String key = getClass().getName() + "." + label;
        if(cachedCommands.containsKey(key)) {
            handled = true;
            // The command has already been stored so use that.
            cachedCommands.get(key).invoke(this);
        } else {
            // Finds the method the first time it has been run.
            for (Method method : getClass().getMethods()) {
                Command cmd = (Command) method.getAnnotation(Command.class);
                // The method is a command if it has a command annotation with the value true;
                if (cmd != null && cmd.value()) {
                    if (method.getName().equals(label)) {
                        // The method matches the commands name.
                        handled = true;
                        // Invoke the method.
                        method.invoke(this);
                        // Cache the method.
                        cacheMethod(method);
                        break;
                    } else {
                        // The method did not match the command's name so check for alias'.
                        Alias alias = method.getAnnotation(Alias.class);
                        if (alias != null) {
                            // The method has alias'.
                            for (String a : alias.value()) {
                                if (a.equals(label)) {
                                    // The alias matched the command's name.
                                    handled = true;
                                    // Invoke the method.
                                    method.invoke(this);
                                    // Cache the method.
                                    cacheMethod(method);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Executes a command for another player.
     * 
     * @param client The client of the other player.
     * @param args The command to execute.
     */
    public boolean executeFor(MapleClient client, String[] args) throws Exception {
        CommandHandler temp = new CommandHandler(client, header, args);
        temp.execute();
        return temp.handled();
    }
    
    /**
     * Sets whether the command has been handled or not.
     * 
     * @param handled True if the command has been handled.
     */
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    /**
     * Used to tell if the command was handled or not by this handler.
     * 
     * @return Returns true if the command was handled during execution.
     */
    public boolean handled() {
        return handled;
    }
    
    /**
     * Caches a method and it's alias'.
     * 
     * @param method The method to cache.
     */
    private void cacheMethod(Method method) throws Exception {
        String name = method.getName();
        if(!cachedCommands.containsKey(name)) {
            cachedCommands.put(getClass().getName() + "." + name, method);
            // Check for alias'.
            Alias alias = method.getAnnotation(Alias.class);
            if (alias != null) {
                // The method has alias'.
                for (String a : alias.value()) {
                    cachedCommands.put(getClass().getName() + "." + a, method);
                }
            }
        }
    }
    
    /**
     * Clears the command cache.
     */
    public static void clearCache() {
        cachedCommands.clear();
    }

    /**
     * Returns true if this command handler has the specified command <code>label</code>.
     * 
     * @param label The command label.
     * @return Returns true if this command handler has the command <code>label</code>.
     */
    protected boolean hasCommand(String label) {
        boolean hasCommand = false;
        try {
            Method method = getClass().getMethod(label);
            Command cmd = (Command) method.getAnnotation(Command.class);
            if (cmd != null && cmd.value()) {
                hasCommand = true;
            }
        } catch (NoSuchMethodException nsme) {
        }
        return hasCommand;
    }

    /**
     * Used to tell if a command has a tag or not.
     * 
     * @param label The command to check.
     * @param tag The tag to confirm.
     * @return Returns true if the tag is on the command.
     */
    protected boolean hasTag(String label, String tag) {
        boolean hasTag = false;
        try {
            Method method = getClass().getMethod(label);
            if (method != null) {
                Tags tags = (Tags) method.getAnnotation(Tags.class);
                if (tags != null) {
                    for (String t : tags.value()) {
                        if (t.equalsIgnoreCase(tag)) {
                            hasTag = true;
                            break;
                        }
                    }
                }
            }
        } catch (NoSuchMethodException nsme) {
        }
        return hasTag;
    }

    /**
     * Gets a command's info.
     * 
     * @param name The name of the command.
     * @return Returns info about the command or an empty class if not found.
     */
    public CommandInfo getCommand(String name) {
        CommandInfo info = new CommandInfo();
        try {
            Method method = getClass().getMethod(name);
            Command cmd = (Command) method.getAnnotation(Command.class);
            if (cmd != null && cmd.value()) {
                info.name = name;
                Syntax syntax = (Syntax) method.getAnnotation(Syntax.class);
                if (syntax != null) {
                    info.syntax = syntax.value();
                }
                Description desc = (Description) method.getAnnotation(Description.class);
                if (desc != null) {
                    info.description = desc.value();
                }
                Alias alias = (Alias) method.getAnnotation(Alias.class);
                if (alias != null) {
                    info.alias = alias.value();
                }
                Tags tags = (Tags) method.getAnnotation(Tags.class);
                if (tags != null) {
                    info.tags = tags.value();
                }
            }
        } catch (NoSuchMethodException nsme) {
        }
        return info;
    }

    /**
     * Gets help for a command handler.
     * 
     * @return Returns a list with each line of command help.
     */
    public List<CommandInfo> getCommandInfo() {
        List<CommandInfo> info = new ArrayList<CommandInfo>();
        try {
            for (Method method : getClass().getMethods()) {
                CommandInfo cmdInfo = new CommandInfo();
                Command cmd = (Command) method.getAnnotation(Command.class);
                if (cmd != null && cmd.value()) {
                    cmdInfo.name = method.getName();
                    Syntax syntax = (Syntax) method.getAnnotation(Syntax.class);
                    if (syntax != null) {
                        cmdInfo.syntax = syntax.value();
                    } else {
                        cmdInfo.syntax = header + method.getName();
                    }
                    Alias alias = (Alias) method.getAnnotation(Alias.class);
                    if (alias != null) {
                        cmdInfo.alias = alias.value();
                    }
                    Tags tags = (Tags) method.getAnnotation(Tags.class);
                    if (tags != null) {
                        cmdInfo.tags = tags.value();
                    }
                    Description desc = (Description) method.getAnnotation(Description.class);
                    if (desc != null) {
                        cmdInfo.description = desc.value();
                        info.add(cmdInfo);
                    }
                }
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        return info;
    }

    /**
     * Warps the character to a certain map on a certain channel.
     * 
     * @param toWarp The character to warp.
     * @param channel The channel to warp to.
     * @param map The map to warp to.
     * @param position The position on the map to warp to.
     */
    public void warpToMapAndChannel(MapleCharacter toWarp, Byte channel, MapleMap map, Point position) {
        
        if (channel != toWarp.getClient().getChannel()) {
            toWarp.message("Changing to channel " + channel + " this may take a few seconds.");
            toWarp.setNextMap(map.getId(), map.findClosestSpawnpoint(position).getId());
            ChangeChannelHandler.getChannelChange(channel, toWarp.getClient());
            toWarp.message("You are being warped to " + map.getMapName() + " (" + map.getStreetName() + ")");
        } else if (toWarp.getMapId() != map.getId()) {
            if (position != null) {
                toWarp.message("You are being warped to " + map.getMapName() + " (" + map.getStreetName() + ")");
                toWarp.changeMap(map, map.findClosestSpawnpoint(position));
            } else {
                toWarp.message("You are being warped to " + map.getMapName() + " (" + map.getStreetName() + ")");
                toWarp.changeMap(map);
            }
        }
    }
    
    /**
     * Warps a character to another character.
     * 
     * @param toWarp The character to warp.
     * @param target The character to target.
     */
    public void warpToCharacter(MapleCharacter toWarp, MapleCharacter target) {
        warpToMapAndChannel(toWarp, target.getClient().getChannel(), target.getMap(), target.getPosition());
    }
}
