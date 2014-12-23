
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Scanner;
import net.server.CreateINI;
import net.server.Server;


public class Main {
    
    private static Server server;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        FileWriter fWriter = new FileWriter("ServerLog.html", true);
//        BufferedWriter out = new BufferedWriter(fWriter);
//        SimpleDateFormat date_format = new SimpleDateFormat("MM/dd, yyyy HH:mm");
//        out.write("<strong>" + date_format.format(new Date()) + "</strong> " + cause.toString() + " - " + cause.getMessage() + "\n<br />\n<ul>\n");
//        for(StackTraceElement ste : cause.getStackTrace()){
//            out.write("<li>" + ste.toString() + "</li>\n");
//        }
//        out.write("</ul>\n<br />\n");
//        out.close();
//        fWriter.close();
        
        server = Server.getInstance();
        System.out.println("Type 'help' for commands.");
        boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        
        while(!exit) {
            System.out.print("Enter a command: ");
            String input = scanner.nextLine().trim();
            if(input.equalsIgnoreCase("start") || input.equalsIgnoreCase("run")) {
                if(serverIsSetup()) {
                    if(!server.isOnline()) {
                        System.out.println("Starting server...");
                        exit = true;
                        Server.getInstance().start();
                    } else {
                        System.out.println("An instance is already running.");
                    }
                } else {
                    System.out.println("The server has not been setup or theres an error in the ini file. Type 'setup'.");
                }
//            } else if(input.equalsIgnoreCase("restart") || input.equalsIgnoreCase("reload")) {
//                if(serverIsSetup()) {
//                        System.out.println("Restarting server...");
//                        Server.restart();
//                } else {
//                    System.out.println("The server has not been setup or theres an error in the ini file. Type 'setup'.");
//                }
//            } else if(input.equalsIgnoreCase("stop") || input.equalsIgnoreCase("shutdown")) {
//                if(serverIsSetup()) {
//                        System.out.println("Stopping server...");
//                        Server.shutdown();
//                        serverThread.interrupt();
//                } else {
//                    System.out.println("The server has not been setup or theres an error in the ini file. Type 'setup'.");
//                }
            }  else if(input.equalsIgnoreCase("setup")) {
                System.out.println("-- Setup --");
                CreateINI.run();
            } else if(input.equalsIgnoreCase("exit")) {
                if(server.isOnline()) {
                    System.out.print("Force shutdown? (Yes|No) ");
                    input = scanner.nextLine();
                    if(input.startsWith("y") || input.startsWith("Y")) {
                        Server.forceShutdown();
                    } else {
                        Server.shutdown();
                    }
                }
                exit = true;
            } else if(input.equalsIgnoreCase("help")) {
                System.out.println("-- Help --");
                System.out.println("start - Starts the server.");
//                System.out.println("restart - Stops and starts the server.");
//                System.out.println("stop - Stops the server.");
                System.out.println("setup - Sets up the server settings.");
                System.out.println("exit - Exits the program.");
            } else {
                System.out.println("Incorrect command, try 'help'.");
            }
        }
        
        System.out.println("Console exited.");
        
        //Server.getInstance().start();
        
//        if(Thread.activeCount() > 1) {
//            System.out.print("There are threads still running, stop them? (Yes|No) ");
//            String input = scanner.nextLine();
//            if(input.startsWith("y") || input.startsWith("Y")) {
//                for(Entry entry : Thread.getAllStackTraces().entrySet()) {
//                    Thread thread = (Thread)entry.getKey();
//                    if(thread != Thread.currentThread() && !thread.getThreadGroup().getName().equals("system")) {
//                        thread.interrupt();
//                        thread.stop();
//                        System.out.println(thread + " has been stopped.");
//                    }
//                }
//            } else {
//                System.out.println("Current threads: ");
//                for(Entry entry : Thread.getAllStackTraces().entrySet()) {
//                    Thread thread = (Thread)entry.getKey();
//                    if(thread != Thread.currentThread() && !thread.getThreadGroup().getName().equals("system")) {
//                        System.out.println(thread);
//                    }
//                }
//                System.out.println("Waiting for threads to finish...");
//            }
//        }
    }
    
    private static boolean serverIsSetup() {
        boolean isSetup = true;
        try {
            Properties p = new Properties();
            FileInputStream fin = new FileInputStream("server.ini");
            p.load(fin);
            p.clear();
            fin.close();
        } catch(Exception e) {
            isSetup = false;
        }
        return isSetup;
    }
}
