package net.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author kevintjuh93
 */
public class CreateINI {
    public static void run() {
        try {
            StringBuilder sb = new StringBuilder();
            String serverName;
            byte worlds;
            Scanner scanner = new Scanner(System.in);
            
            String input = "";
            
            sb.append("#Do NOT modify unless you know what your doing.\r\n");
            sb.append("#Flag types: 0 = nothing, 1 = event, 2 = new, 3 = hot\r\n\r\n");
            
            System.out.println("Enter 'cancel' anytime to cancel setup.");
            
            System.out.print("Server name: ");
            input = scanner.nextLine().trim();
            if(input.equalsIgnoreCase("cancel")) return;
            serverName = input;
            sb.append("servername=").append(serverName).append("\r\n");

            System.out.println("Flag types: 0 = nothing, 1 = event, 2 = new, 3 = hot\r\n");

            System.out.print("Number of worlds: ");
            input = scanner.nextLine().trim();
            if(input.equalsIgnoreCase("cancel")) return;
            worlds = Byte.parseByte(input);
            sb.append("worlds=").append(worlds).append("\r\n\r\n");

            System.out.println("\r\n");


            for (byte b = 0; b < worlds; b++) {
                sb.append("#Properties for world ").append(b).append("\r\n");

                System.out.println("Properties for world " + b);
                if (b > 2) System.out.println("Make sure you create a npc folder for this world!");
                System.out.print("   Flag: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("flag").append(b).append("=").append(Byte.parseByte(input)).append("\r\n");

                System.out.print("   Event message: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("eventmessage").append(b).append("=").append(input).append("\r\n");

                System.out.print("   Number of channels: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("channels").append(b).append("=").append(Byte.parseByte(input)).append("\r\n");

                System.out.print("   Exp rate: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("exprate").append(b).append("=").append(Short.parseShort(input)).append("\r\n");

                System.out.print("   Meso rate: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("mesorate").append(b).append("=").append(Short.parseShort(input)).append("\r\n");

                System.out.print("   Drop rate: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("droprate").append(b).append("=").append(Short.parseShort(input)).append("\r\n");

                System.out.print("   Boss drop rate: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("bossdroprate").append(b).append("=").append(Short.parseShort(input)).append("\r\n");

                System.out.print("   NX rate: ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("nxrate").append(b).append("=").append(Short.parseShort(input)).append("\r\n");

                System.out.print("   Channel respawn rate: (Default: 3000) ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                sb.append("respawnrate").append(b).append("=").append(Short.parseShort(input)).append("\r\n");

                System.out.println("\r\n");
                sb.append("\r\n");
            }

            System.out.print("Do you want a GM Server? (true/false) ");
            input = scanner.nextLine().trim();
            if(input.equalsIgnoreCase("cancel")) return;
            sb.append("\r\n").append("gmserver=").append(Boolean.parseBoolean(input));
            FileOutputStream out = null;
            try {
                out = new FileOutputStream("server.ini", false);
                out.write(sb.toString().getBytes());
            } catch (IOException ioe) {
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException ioe) {
                }
            }

            sb = new StringBuilder();
            try {
                System.out.println("\r\nYou are about to set the Java Heap Size, if you don't know what it is, type '?'.");
                System.out.print("Java Heap Size (in MB): ");
                input = scanner.nextLine().trim();
                if(input.equalsIgnoreCase("cancel")) return;
                String heapsize = input;
                while (heapsize.equals("?")) {
                    System.out.println("\r\n");
                    System.out.println("WikiAnswers: Java heap is the heap size allocated to JVM applications which takes care of the new objects being created. If the objects being created exceed the heap size, it will throw an error saying memory Out of Bound\r\n\r\n");
                    System.out.print("Java Heap Size (in MB): ");
                    input = scanner.nextLine().trim();
                    if(input.equalsIgnoreCase("cancel")) return;
                    heapsize = input;
                }
                String osName = System.getProperty("os.name");
                if (osName.startsWith("Windows")) {
                    out = new FileOutputStream("launch_server.bat", false);
                    sb.append("@echo off").append("\r\n").append("@title ").append(serverName).append("\r\n");
                    sb.append("set CLASSPATH=.;dist\\*\r\n");
                    sb.append("java -Xmx").append(heapsize).append("M -Dwzpath=wz\\ -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd Main\r\n");
                    sb.append("pause");
                } else if(osName.startsWith("Linux")){
                    out = new FileOutputStream("launch_server.sh", false);
                    sb.append("#!/bin/sh").append("\r\n\r\n");
                    sb.append("export CLASSPATH=").append(".:dist//*\r\n\r\n");
                    sb.append("java -Dwzpath=wz/ \\ \r\n").append("-Djavax.net.ssl.keyStore=filename.keystore \\ \r\n-Djavax.net.ssl.keyStorePassword=passwd \\ \r\n-Djavax.net.ssl.trustStore=filename.keystore \\ \r\n-Djavax.net.ssl.trustStorePassword=passwd \\ \r\n");
                    sb.append("-Xmx").append(heapsize).append("M \\").append("\r\nMain");                
                } else if(osName.startsWith("Mac")){
                    out = new FileOutputStream("launch_server.command", false);
                    sb.append("cd '").append(System.getProperty("user.dir")).append("'\r\n");
                    sb.append("export CLASSPATH=./dist/*\r\n");
                    
                    sb.append("java -Xmx").append(heapsize).append("M ");
                    sb.append("-Dwzpath=wz/ ");
                    sb.append("-Djavax.net.ssl.keyStore=filename.keystore ");
                    sb.append("-Djavax.net.ssl.keyStorePassword=passwd ");
                    sb.append("-Djavax.net.ssl.trustStore=filename.keystore ");
                    sb.append("-Djavax.net.ssl.trustStorePassword=passwd ");
                    sb.append(" Main");
                } else {
                    out = new FileOutputStream("launch_server.sh", false);
                    sb.append("#!/bin/sh").append("\r\n\r\n");
                    sb.append("export CLASSPATH=").append(".:dist//*\r\n\r\n");
                    sb.append("java -Dwzpath=wz/ \\ \r\n").append("-Djavax.net.ssl.keyStore=filename.keystore \\ \r\n-Djavax.net.ssl.keyStorePassword=passwd \\ \r\n-Djavax.net.ssl.trustStore=filename.keystore \\ \r\n-Djavax.net.ssl.trustStorePassword=passwd \\ \r\n");
                    sb.append("-Xmx").append(heapsize).append("M \\").append("\r\nMain");                
                }
                out.write(sb.toString().getBytes());
            } catch (IOException ioe) {
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException ioe) {
                }
            }
            System.out.println("\r\nSetup complete.\r\nMake sure that ServerConstants.java is modified too, and clean+compiled before you start the server.");
        } catch (Exception ex) {
            System.out.println("You entered the wrong information. Please follow the instructions.");
            
        }
    }
}
