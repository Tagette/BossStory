
import net.server.Server;


public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("-- BossStory Server --");
        Server.getInstance().start();
    }
}
