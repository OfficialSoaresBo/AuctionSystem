public interface AuctionInterface 
          extends java.rmi.Remote {	

    //Creates new auction with details passed.
    public String createAuction(double startPrice, double minPrice, StringBuilder desc, String auctionOwner)
        throws java.rmi.RemoteException;

    //returns bolean value if auction exists or not
    public boolean findAuction(String aucID)
        throws java.rmi.RemoteException;

    //Displays auction details
    public StringBuilder viewDetails(String aucID)
        throws java.rmi.RemoteException;

    //Removes auction passed
    public boolean deleteAuction(String aucID)
        throws java.rmi.RemoteException;

    //Shows all available auctions
    public StringBuilder viewAllAuctions()
        throws java.rmi.RemoteException;

    //Shows current highest bid for auction queried
    public double viewCurrentBid(String aucID)
        throws java.rmi.RemoteException;

    //Adds new bid to auction passed
    public boolean addNewBid(String aucID, double newBid, String name, String email, String uID)
        throws java.rmi.RemoteException;

    //Returns String with winner username
    public String getWinner(String aucID)
        throws java.rmi.RemoteException;

    //Returns Stringbuilder with user details
    public StringBuilder viewWinnerDetails(String aucID)
        throws java.rmi.RemoteException;

    //Returns bolean if reserve has been achieved or not
    public boolean reserveAchieved(String aucID)
    	throws java.rmi.RemoteException;

    //Returns bolean if username is owner of auction
    public boolean isOwner(String aucID, String username)
        throws java.rmi.RemoteException;

    public byte[] getPKey()
        throws java.rmi.RemoteException;

    public byte[] getSig(String challenge)
        throws java.rmi.RemoteException;

    public boolean addUserKey(String uID, byte[] pubKey)
        throws java.rmi.RemoteException;
    
    public String getServerChallenge(String uID)
        throws java.rmi.RemoteException;
    
    public boolean verifySrvSig(byte[] signature, String uID)
        throws java.rmi.RemoteException;
        

    //Checks if username exists
    public boolean hasUser(String username)
        throws java.rmi.RemoteException;

    //Add new user
    public boolean addUser(String name, String email, String uID)
        throws java.rmi.RemoteException;
}