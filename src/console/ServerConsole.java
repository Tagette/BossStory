/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package console;

import net.server.Server;

/**
 *
 * @author Tristan
 * @date Mar 16, 2012
 */
public class ServerConsole {
    
    private Thread serverThread;
    private boolean serverOnline;
    private CommandReader reader;
    private OutputHandler outHandle;
    private boolean exitApplication;
    
    public ServerConsole(){
        
    }
    
    public void start(){
        reader = new CommandReader(System.in);
        //outHandle =  new OutputHandler(System.out, reader);
        //System.setOut(new PrintStream(outHandle));
        
        Runnable runServer = new Runnable(){
            @Override
            public void run() {
                Server.getInstance().start();
            }
        };
        serverThread = new Thread(runServer);
        serverThread.start();

        exitApplication = false;
        serverOnline = false;
        
        // TODO: create thread for input
        Thread inputThread = new Thread(reader);
        inputThread.setName("InputReader");
        inputThread.start();
        
        while(!exitApplication){
            String input = reader.getLine();
            if(input != null){
                processCommand(input);
            }
        }
    }
    
    private void startServer(){
        if(!serverOnline){
            println("Server starting...");
            serverThread.start();
            serverOnline = true;
        }
    }
    
    private void stopServer(){
        if(serverOnline){
            println("Server is shutting down...");
            Server.getInstance().shutdown();
            serverOnline = false;
        }
    }
    
    private void println(String message){
        System.out.println(message);
    }
    
    private void processCommand(String input){
        String[] split = input.split(" ");
        if(split[0].equalsIgnoreCase("start")){
            startServer();
        } else if(split[0].equalsIgnoreCase("help")
                || split[0].equalsIgnoreCase("?")){
            println("-- Help --");
            println("start - Starts the server.");
            println("stop  - Stops the server.");
            println("exit  - Stops the server and exits.");
        } else if(split[0].equalsIgnoreCase("stop")){
            stopServer();
        } else if(split[0].equalsIgnoreCase("exit")){
            stopServer();
            exitApplication = true;
        } else {
            println("Unknown command: " + input);
        }
    }
    
}
