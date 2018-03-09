import java.net.MalformedURLException;
import java.rmi.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.security.*;
import java.security.spec.*;

public class BuyerClient {
    private static Scanner scan = new Scanner(System.in);
    private static AuctionInterface auction = null;
    private static Console console = System.console();
    private static String name, email, uID ="";

    private static Utilities util = new Utilities();
    private static String challenge = "";
    private static KeyPair pair = null;
    private static PrivateKey priv = null;
    private static PublicKey pub = null;
    private static byte[] pubKey = null;

    public static void main(String[] args) {
        if (genKeys()) {
            System.out.println("All settings loaded");
            System.out.println("Welcome to the Auction System Buyer Program\n");
            
            util.printScreen("What's Your Username");
            uID = scan.next();

            try{
                auction = (AuctionInterface)Naming.lookup("rmi://localhost/AuctionService");
                if (auction.hasUser(uID)) {
                    System.out.println("User was found");
                    serverAuth();
                }
                else{
                    System.out.println("User was not found");
                    newUser(uID);
                }
            }
            catch (ConnectException ce){
                System.out.println("Please Make sure AuctionServer is running");
            }
            catch (MalformedURLException murle) {
                System.out.println();
                System.out.println("MalformedURLException");
                System.out.println(murle);
            }
            catch (RemoteException re) {
                System.out.println();
                System.out.println("RemoteException");
                System.out.println(re);
            }
            catch (NotBoundException nbe) {
                System.out.println();
                System.out.println("NotBoundException");
                System.out.println(nbe);
            }
            catch (Exception e) {
                System.out.println(e);
            }
            /*
            util.printScreen("Enter your secret password: ");
            char newPassArray[] = console.readPassword();
            System.out.println(uID + new String(newPassArray));
            */
           
        } else{
            System.out.println("Keys couldn't be generated correctly");
        }
    }

    private static void clientAuth(){
        try{
            if (auction.addUserKey(uID,pubKey)) {
                System.out.println("Server Has Received User Public Key");
                String serverChallenge = auction.getServerChallenge(uID);
                System.out.println("Server Challenge Has been siged");
                byte[] signedChallenge = signChallenge(serverChallenge);
                if (auction.verifySrvSig(signedChallenge, uID)) {
                    System.out.println("Both Parties Have been Verified Succesfully");
                    authorized(uID);
                }
            }else {
                System.out.println("User Key wasn't received by the server");
            }
        }catch (Exception e) {
            System.out.println(e);
        }
    }

    private static byte[] signChallenge( String srvChallenge){
        byte[] realSig = null;
        try{
            Signature dsa = Signature.getInstance("SHA1withDSA"); 
            dsa.initSign(priv);
            dsa.update(srvChallenge.getBytes());
            realSig =  dsa.sign();
        }catch (Exception e) {
            System.out.println(e);
        }
        return realSig;
    }

    /**
     * Authenticate Server
     */
    private static void serverAuth(){
        try{
            if (getServerPubKey()) {
                System.out.println("Server Public Key Obtained");
                if (challengeServer()) {
                    System.out.println("Server challenge Signature Received");
                    if (verifySig()) {
                        clientAuth();
                    }else {
                        System.out.println("Server wasn't Verified");
                    }
                }else {
                    System.out.println("Signature wasn't obtained");
                }
            }else{
                System.out.println("Server Public Key Couldn't have been retrieved");
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Generate Public and Private Key
     * @return [description]
     */
    private static boolean genKeys(){
        boolean done = false;
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            keyGen.initialize(1024, random);
            pair = keyGen.generateKeyPair();
            priv = pair.getPrivate();
            pub = pair.getPublic();

            //Save public key to byte Array
            pubKey = pub.getEncoded();
            System.out.println("User Generated Public Key");
            done = true;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return done;
    }

    /**
     * Verifies Server Signature
     * @return [description]
     */
    private static boolean verifySig(){
        boolean done = false;
        try{/* Import Encoded public key */
            FileInputStream keyfis = new FileInputStream("serverPubKey");
            byte[] encKey = new byte[keyfis.available()];  
            keyfis.read(encKey);
            keyfis.close();

            //Loads pubKey received from server
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            //loads signatured received from server
            FileInputStream sigfis = new FileInputStream("serverChalSig");
            byte[] sigToVerify = new byte[sigfis.available()]; 
            sigfis.read(sigToVerify);
            sigfis.close();

            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(pubKey);

            sig.update(challenge.getBytes());

            boolean verifies = sig.verify(sigToVerify);
            System.out.println("Server Signature verifies: " + verifies);
            
            done = true;
        }catch (Exception e) {
            System.out.println(e);
        }

        return done;
    }

    /**
     * Challenge Server
     * @return [description]
     */
    private static boolean challengeServer(){
        boolean done = false;
        try{
            challenge = util.genRandID();
            byte[] signature = auction.getSig(challenge);
            FileOutputStream sigfos = new FileOutputStream("serverChalSig");
            sigfos.write(signature);
            sigfos.close();
            done = true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return done;
    }

    /**
     * Gets Server Public Key
     * @return [description]
     */
    private static boolean getServerPubKey(){
        boolean done = false;
        try{       
            byte[] pubKey = auction.getPKey();
            FileOutputStream keyfos = new FileOutputStream("serverPubKey");
            keyfos.write(pubKey);
            keyfos.close();
            done = true;
        } catch(Exception e){
            System.out.println(e);
        }
        return done;
    }

    /**
     * Authorized method once the authorization goes through
     * @param uID [description]
     */
    private static void authorized(String uID){
        util.printScreen("Welcome " + uID);
        while (true){
            try {
                util.printScreen("1 - Browse All Auctions");
                util.printScreen("2 - Place New Bid");
                util.printScreen("3 - View Previous Bid");
                if (scan.hasNextInt()) {
                    int method = scan.nextInt();
                    switch(method){
                        case 1:
                            util.printScreen("All Available Auctions");
                            if (auction.viewAllAuctions().length() > 0) {
                                System.out.println(auction.viewAllAuctions());
                            }else{
                                System.out.println("No Available Auctions");
                            }
                            break;

                        case 2:
                            util.printScreen("What's the Auction ID");
                            String auctionID = scan.next();
                            if (auction.findAuction(auctionID)) {
                                if (!auction.isOwner(auctionID, uID)) {
                                    util.printScreen("The current bid for item: #" + auctionID);
                                    double currentBid = auction.viewCurrentBid(auctionID);
                                    System.out.println(util.currencyFormat(currentBid));

                                    util.printScreen("What's your new Bid");
                                    double newBid = scan.nextInt();
                                    if (newBid > currentBid) {
                                        if (auction.addNewBid(auctionID,newBid,name,email,uID)) {
                                                System.out.println("Bid Submitted Succesfully");
                                                System.out.println("New Highest Bid: " + util.currencyFormat(auction.viewCurrentBid(auctionID)));
                                            }
                                        else{
                                            System.out.println("Something went wrong, bid wasn't submitted please try again");
                                        }   
                                    }
                                    else{
                                        System.out.println("The new Bid must be higher than the current bid: " + util.currencyFormat(currentBid));
                                    }
                                }
                                else{
                                    System.out.println("You can't bid on your own auctions");
                                }
                            }
                            else{
                                System.out.println("That Auction ID could not be found");
                            }
                            break;

                        case 3:
                            util.printScreen("Previous Bid");
                            //System.out.println(auction.viewAllBids(uID));
                            break;
                        
                        default :
                            System.out.println("\nThe options are: \n'1' Browse All Auctions \n '2' Place New Bid \n '3' See previous Bid \n");
                    }
                }
                else{
                    System.out.println("Please only input integers");
                    System.out.println("Special characters are not accepted '/?-!@£$%^&*(' ");
                }
            }
            // Catch the exceptions that may occur - rubbish URL, Remote exception
            catch (ConnectException ce){
                System.out.println("Please Make sure AuctionServer is running");
            }

            catch (RemoteException re) {
                System.out.println();
                System.out.println("RemoteException");
                System.out.println(re);
            }
        }
    }

    /**
     * Gets Description from user and creates Stringbuilder with it.
     * Until user has finishes typing it and finishes with "."
     */
    private static StringBuilder getDesc(){
        util.printScreen("What's the Description");
        System.out.println("Finisht with '.'");
        StringBuilder descript = new StringBuilder();

        while(true){
            String newLine = scan.next();
            String lastChar = newLine.substring(newLine.length() -1);

            if (lastChar.equals(".")) {
                descript.append(newLine);
                break;
            }
            else{
                descript.append(newLine);
                descript.append(System.getProperty("line.separator"));
            }
        }
        return descript;
    }

    /**
     * Creates a new User
     * @param uID [description]
     */
    private static void newUser(String uID) {
        try{
            System.out.println("Create a new User to use the application");
            util.printScreen("What's Your First Name");
            name = scan.next();
            
            util.printScreen("What's Your Email");
            email = scan.next();
            
            if(util.validEmail(email)) {
                System.out.println("\nName: " + name);
                System.out.println("Email: " + email);
                if (auction.addUser(name,email,uID)) {
                    System.out.println("\nUser Created Succesfully");
                    authorized(uID);
                }
                else{
                    System.out.println("Something went wrong, user wasn't created please try again");
                }
            }
            else{
                System.out.println("Invalid Email");
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}

