import java.rmi.Naming;	//Import naming classes to bind to rmiregistry
import java.rmi.ConnectException;


public class AuctionServer {
  
  /**
   * Starts server by binding name interface object to rmi
   * @param args [description]
   */
  public static void main(String[] args) {
    try {
      AuctionInterface auction = new AuctionImpl();
      Naming.rebind("rmi://localhost/AuctionService", auction);
      System.out.println("\nServer Setup Completed Sucessfuly");
    }
    catch (ConnectException ce){
      System.out.println("Please Make sure rmiregistry is running");
    }
    catch (Exception e){
      System.out.println("Server Error: " + e);
    }
  }
}