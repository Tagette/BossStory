/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.server;

import client.MapleCharacter;
import constants.ServerConstants;
import java.io.File;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import net.MaplePacket;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.mina.MapleCodecFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.TimerManager;
import server.events.gm.MapleEvent;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;

public class Channel {
    private int port = 7575;
    private PlayerStorage players = new PlayerStorage();
    private byte world, channel;
    private IoAcceptor acceptor;
    private String ip;
    private boolean shutdown = true;
    private boolean finishedShutdown = false;
    private MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private Map<Integer, HiredMerchant> hiredMerchants = new HashMap<Integer, HiredMerchant>();
    private MapleEvent event;
    private String channelMessage;
    private Thread shutdownHook;

    public Channel(final byte world, final byte channel, Properties ini) {
        this.world = world;
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), world, channel);

        this.channelMessage = ServerConstants.SERVER_MESSAGE;
        shutdownHook = new Thread() {
            @Override
            public void run() {
            	System.out.println("Saving channel " + channel + " characters...");
                    for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
                        mc.saveToDB(true);
                        if (mc.getHiredMerchant() != null) {
                            if (mc.getHiredMerchant().isOpen()) {
                                try {
                                    mc.getHiredMerchant().saveItems(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try {
            eventSM = new EventScriptManager(this, ServerConstants.EVENTS.split(" "));
            port = 7575 + this.channel - 1;
            port += (world * 100);
            ip = ServerConstants.HOST + ":" + port;
            IoBuffer.setUseDirectBuffer(false);
            IoBuffer.setAllocator(new SimpleBufferAllocator());
            acceptor = new NioSocketAcceptor();
            Runnable respawnMaps = new Runnable() {
                @Override
                public void run() {
                    for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
                        map.getValue().respawn();
                    }
                }
            };
            TimerManager.getInstance().register(respawnMaps, Short.parseShort(ini.getProperty("respawnrate" + world)));
            acceptor.setHandler(new MapleServerHandler(PacketProcessor.getProcessor(), channel, world));
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
            acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            
            eventSM.init();
            shutdown = false;
            System.out.println("    Channel " + getId() + ": Listening on port " + port);
        } catch (BindException be) {
            System.out.println("    Unable to start channel " + getId() + ". Port already in use.");
        } catch (NumberFormatException nfe) {
            System.out.println("    Unable to start channel " + getId() + ". Error in ini file. Type 'setup'.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
        boolean error = true;
        while (error) {
            try {
                for (HiredMerchant hm : getHiredMerchants().values()) {
                    hm.saveItems(true);
                }
                for (MapleCharacter chr : players.getAllCharacters()) {
                    synchronized (chr) {
                        chr.saveToDB(true);
                        chr.getClient().disconnect();
                    }
                    error = false;
                }
            } catch (Exception e) {
                error = true;
            }
        }
        acceptor.unbind();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        finishedShutdown = true;
    }
    
    public void forceShutdown() {
        shutdown = true;
        for(MapleCharacter chr : players.getAllCharacters()) {
            chr.getClient().disconnect();
        }
        acceptor.unbind();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        finishedShutdown = true;
    }

    public void unbind() {
        acceptor.unbind();
    }

    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public int getWorld() {
        return world;
    }

    public void addPlayer(MapleCharacter chr) {
        players.addPlayer(chr);
        chr.announce(MaplePacketCreator.serverMessage(channelMessage));
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        players.removePlayer(chr.getId());
    }

    public int getConnectedClients() {
        return players.getAllCharacters().size();
    }

    public void setChannelMessage(String newMessage) {
        channelMessage = newMessage;
        broadcastPacket(MaplePacketCreator.serverMessage(channelMessage));
    }

    public void broadcastPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }

    public final byte getId() {
        return channel;
    }

    public String getIP() {
        return ip;
    }

    public boolean isShutdown() {
        return shutdown;
    }

//    public void shutdown(int time) {
//        // TimerManager.getInstance().schedule(new ShutdownServer(getChannel()), time);
//    }

    public MapleEvent getEvent() {
        return event;
    }

    public void setEvent(MapleEvent event) {
        this.event = event;
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public void broadcastGMPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.isGM()) {
                chr.announce(data);
            }
        }
    }

    public void yellowWorldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.announce(MaplePacketCreator.sendYellowTip(msg));
        }
    }

    public void worldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.message(msg);
        }
    }

    public List<MapleCharacter> getPartyMembers(MapleParty party) {
        List<MapleCharacter> partym = new ArrayList<MapleCharacter>(8);
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == getId()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    partym.add(chr);
                }
            }
        }
        return partym;
    }

    public class respawnMaps implements Runnable {
        @Override
        public void run() {
            for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
                map.getValue().respawn();
            }
        }
    }

    public Map<Integer, HiredMerchant> getHiredMerchants() {
        return hiredMerchants;
    }

    public void addHiredMerchant(int chrid, HiredMerchant hm) {
        hiredMerchants.put(chrid, hm);
    }

    public void removeHiredMerchant(int chrid) {
        hiredMerchants.remove(chrid);
    }  
    
    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<Integer> ret = new ArrayList<Integer>(characterIds.length);
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : characterIds) {
            MapleCharacter chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(charIdFrom)) {
                    ret.add(characterId);
                }
            }
        }
        int[] retArr = new int[ret.size()];
        int pos = 0;
        for (Integer i : ret) {
            retArr[pos++] = i.intValue();
        }
        return retArr;
    }
}