// The implementation Class must implement the rmi interface (Auction)
// and be set as a Remote object on a server

import java.util.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

public class AuctionImpl
    extends java.rmi.server.UnicastRemoteObject
    implements AuctionInterface {

    private HashMap<String, Auction> auctionsMap = new HashMap<String, Auction>();
    private HashMap<String, User> usersMap = new HashMap<String, User>();
    private HashMap<String, byte[]> keysMap = new HashMap<String, byte[]>();
    private HashMap<String, String> challengesMap = new HashMap<String, String>();
    private KeyPair pair = null;
    private PrivateKey priv = null;
    private PublicKey pub = null;

    private byte[] srvPubKey = null;
    private static String srvChallenge = ""; 

    public AuctionImpl()
        throws java.rmi.RemoteException {
            super();

            Timer t = new Timer( );
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    challengesMap.clear();
                    System.out.println("Challenges Cash Was Cleared");
                }
            }, 0,60000);

            try{
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

                keyGen.initialize(1024, random);
                pair = keyGen.generateKeyPair();
                priv = pair.getPrivate();
                pub = pair.getPublic();

                //Save public key to byte Array
                srvPubKey = pub.getEncoded();
                System.out.println("Server Generated Public Key");
            }
            catch (Exception e) {
                System.out.println(e);
            }
    }

    /**
     * Get Server Challenge
     * @param  uID                      [description]
     * @return                          [description]
     * @throws java.rmi.RemoteException [description]
     */
    public String getServerChallenge(String uID)
        throws java.rmi.RemoteException{
            String temp = genRandID();
            challengesMap.put(uID,temp);
            
            return temp;
        }

    /**
     * Verifies Server Signature
     * @param  signedChallenge          [description]
     * @param  uID                      [description]
     * @return                          [description]
     * @throws java.rmi.RemoteException [description]
     */
    public boolean verifySrvSig(byte[] signedChallenge, String uID)
        throws java.rmi.RemoteException{
            boolean done = false;
            try{    
                byte[] encKey = keysMap.get(uID);  

                //Loads pubKey received from server
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
                KeyFactory keyFactory = KeyFactory.getInstance("DSA");
                PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

                Signature sig = Signature.getInstance("SHA1withDSA");
                sig.initVerify(pubKey);

                sig.update(challengesMap.get(uID).getBytes());

                boolean verifies = sig.verify(signedChallenge);
                System.out.println("Server Signature verifies: " + verifies);
                done = verifies;
            }catch (Exception e) {
                System.out.println(e);
            }
            return done;
        }

    /**
     * Adds This User Public Key to the server
     * @param  username                 [description]
     * @param  userPubKey               [description]
     * @return                          [description]
     * @throws java.rmi.RemoteException [description]
     */
    public boolean addUserKey(String username, byte[] userPubKey)
        throws java.rmi.RemoteException{
        keysMap.put(username,userPubKey);
        return true;
    }

    /**
     * Get user sig
     * @return [description]
     */
    public byte[] getSig( String usrChallenge)
        throws java.rmi.RemoteException{
        byte[] realSig = null;
        try{
            Signature dsa = Signature.getInstance("SHA1withDSA"); 
            dsa.initSign(priv);

            dsa.update(usrChallenge.getBytes());
            realSig =  dsa.sign();
        }catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Server Responded to challenge");
        return realSig;
    }

    /**
     * get user key
     * @return [description]
     */
    public byte[] getPKey()
        throws java.rmi.RemoteException{
        System.out.println("Server Public Key was requested");
        return srvPubKey;
    }
    
    /**
     * Creates Auction by adding a value to the Hashmap and attahing an id to it which is the random number generated.
     * @param  startPrice               [description]
     * @param  minPrice                 [description]
     * @param  desc                     [description]
     * @return                          [description]
     * @throws java.rmi.RemoteException [description]
     */
    public String createAuction(double startPrice, double minPrice, StringBuilder desc , String auctionOwner)
        throws java.rmi.RemoteException{
        System.out.println("Generating Auction ID");
        String id = genRandID();
        System.out.println("Check if id: #" + id + " is free to be assigned");
        while(auctionsMap.containsKey(id)){
            System.out.println("Match found so generate new id");
            id = genRandID();
        }

        System.out.println("Create new Auction with details £" + startPrice + " £" + minPrice + " " + desc + " #" + id);
        auctionsMap.put(id,new Auction(startPrice,minPrice,desc,auctionOwner));

        return id;
    }

    /**
     * generates random auction 4 digit ID
     * @return [description]
     */
    private String genRandID()
        throws java.rmi.RemoteException{
        Random rand = new Random();
        String randId = String.format("%04d", rand.nextInt(10000));
        return randId;
    }

    /**
     * Checks to see if auction ID is valid or not
     * @param  id [description]
     * @return    [description]
     */
    public boolean findAuction(String id)
        throws java.rmi.RemoteException{        
        System.out.println("Searching for: #" + id);
        boolean res = false;

        if (auctionsMap.containsKey(id)){
            res = true;
            System.out.println("Found: #" + id);
        }
            
        else{
            res = false;
            System.out.println("Didn't find: #" + id);
        }
            
        return res;
    }

    /**
     * Searches the Hashmap for auction containing specific key and builds a string containing the results
     * @param  id [description]
     * @return    [description]
     */
    public StringBuilder viewDetails(String id)
        throws java.rmi.RemoteException{
        StringBuilder res = new StringBuilder();
        Auction temp = auctionsMap.get(id);

        res.append("The owner of this item is:");
            res.append(temp.getOwner());
        res.append(System.getProperty("line.separator"));

        res.append("The Starting Price is: £");
            res.append(temp.getStartPrice());
        res.append(System.getProperty("line.separator"));

        res.append("The Minimum Price is: £");
            res.append(temp.getMinPrice());
        res.append(System.getProperty("line.separator"));

        res.append("The Description is: ");
            res.append(temp.getDesc());

        return res;
    }

    /**
     * Finds auction in the hashmap and deletes it
     * @param  id [description]
     * @return    [description]
     */
    public boolean deleteAuction(String id)
        throws java.rmi.RemoteException{
        boolean res = false;

        if (findAuction(id)) {
            auctionsMap.remove(id);
            System.out.println("Deleted: #" + id);
            res = true;
        }
        else {
            System.out.println("Couldn't Find: #" + id);
            res = false;
        }
        return res;
    }

    /**
     * Returns String containing the keys
     * @return [description]
     */
    public StringBuilder viewAllAuctions()
        throws java.rmi.RemoteException{
        StringBuilder res = new StringBuilder();
        Set <String> openAuctions = auctionsMap.keySet();

        for (String key : openAuctions) {
            String newLine = "#" + key +" Current Bid: £" + auctionsMap.get(key).getCurrentPrice();
            res.append(newLine);
            res.append(System.getProperty("line.separator"));
        }

        return res;
    }

    /**
     * Returns Current Bid
     * @param  id [description]
     * @return    [description]
     */
    public double viewCurrentBid(String id)
        throws java.rmi.RemoteException{
        return auctionsMap.get(id).getCurrentPrice();
    }

    /**
     * Adds new bid and creates new buyer and stores him into buyer hash
     * @param  id     [description]
     * @param  newBid [description]
     * @param  name   [description]
     * @param  email  [description]
     * @return        [description]
     */
    public boolean addNewBid(String aucId, double newBid, String name, String email, String uid)
        throws java.rmi.RemoteException{
        auctionsMap.get(aucId).addNewBid(newBid, uid);
        
        User newBuyer = new User(name,email,uid);
        newBuyer.regBid(aucId,newBid);
        usersMap.put(uid,newBuyer);
        System.out.println("New bid of £" + newBid + " added by " + uid);
        
        return true;
    }

    /**
     * Returns username of winne
     * @param  aucID [description]
     * @return       [description]
     */
    public String getWinner(String aucID)
        throws java.rmi.RemoteException{
        return auctionsMap.get(aucID).getCurrentBider();
    }

    /**
     * Return StringBuilder wit
     * @param  uid                      [description]
     * @return                          [description]
     * @throws java.rmi.RemoteException [description]
     */
    public StringBuilder viewWinnerDetails(String uid)
        throws java.rmi.RemoteException{
        StringBuilder res = new StringBuilder();
        User temp = usersMap.get(uid);

        res.append("Name: ");
            res.append(temp.getName());
        res.append(System.getProperty("line.separator"));

        res.append("Email: ");
            res.append(temp.getEmail());
        res.append(System.getProperty("line.separator"));

        return res;
    }

    /**
     * Checks if seserve price for auction has been achieved
     * @param  aucID [description]
     * @return       [description]
     */
    public boolean reserveAchieved(String aucID)
        throws java.rmi.RemoteException{
    	boolean achieved = false;

    	if (auctionsMap.get(aucID).getMinPrice() < auctionsMap.get(aucID).getCurrentPrice()) {
    		achieved = true;
    	}

    	return achieved;
    }

    /**
     * Checks if username is the owner of the auction passed as parameter
     * @param  aucID    [description]
     * @param  username [description]
     * @return          [description]
     */
    public boolean isOwner(String aucID, String username)
        throws java.rmi.RemoteException{
        boolean isOwner = false;
        if (auctionsMap.get(aucID).getOwner().equals(username)) {
            isOwner = true;
        }

        return isOwner;
    }

    /**
     * Check if username exists
     * @param  username [description]
     * @return          [description]
     */
    public boolean hasUser(String username)
        throws java.rmi.RemoteException{
        boolean foundUser = false;
        if (usersMap.containsKey(username)) {
            foundUser = true;
            System.out.println("Found user: " + username);
        }
        else{
            System.out.println("Couldn't find user: " + username);
        }
        return foundUser;
    }

    /**
     * Add new user to the system
     * @param  name  [description]
     * @param  email [description]
     * @param  uID   [description]
     * @return       [description]
     */
    public boolean addUser(String name, String email, String uID)
        throws java.rmi.RemoteException{
        boolean userAdded = false;
        
        if (!hasUser(uID)) {
            User newUser = new User(name,email,uID);
            usersMap.put(uID,newUser);
            if (hasUser(uID)) {
                userAdded = true;
                System.out.println("User: " + name + " was succesfully added");
            }
        }
        else{
            System.out.println("User already exists");
        }

        return userAdded;
    }
}


