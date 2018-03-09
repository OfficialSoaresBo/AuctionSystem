import java.net.MalformedURLException;
import java.rmi.*;
import java.util.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

public class SellerClient {
    private static Scanner scan = new Scanner(System.in);
    private static AuctionInterface auction = null;
    private static Console console = System.console();
    private static double strtPrice = 0, rsrvPrice = 0;
    private static String  name, email, auctionID, uID = "";
    
    private static Utilities util = new Utilities();
    private static String challenge = "";
    private static KeyPair pair = null;
    private static PrivateKey priv = null;
    private static PublicKey pub = null;
    private static byte[] pubKey = null;

    public static void main(String[] args) {
        if (genKeys()) {
            System.out.println("All settings loaded");
            System.out.println("Welcome to the Auction System Seller Program\n");
            
            util.printScreen("What's Your Username");
            uID = scan.next();

            try{
                auction = (AuctionInterface)Naming.lookup("rmi://localhost/AuctionService");
                if (auction.hasUser(uID)) {
                    System.out.println("User was found");
                    serverAuth();
                }else{
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
        while(true){
            try{
                util.printScreen("1 - Create new Auction");
                util.printScreen("2 - View existing Auction");
                util.printScreen("3 - Close existing Auction");
                util.printScreen("4 - View User Details");
                if (scan.hasNextInt()) {
                    int method = scan.nextInt();
                    switch(method){
                        case 1:
                            boolean repeat = true;
                            while(repeat){
                                util.printScreen("What's the Starting Price ");
                                strtPrice = scan.nextDouble();
                                
                                util.printScreen("What's the Reserve Price Price ");
                                rsrvPrice = scan.nextDouble();

                                if (strtPrice > rsrvPrice) {
                                    System.out.println("Reserve Can't be Lower than Start Price");
                                    break;
                                }

                                if (!util.validNum(strtPrice) || !util.validNum(rsrvPrice)) {
                                    System.out.println("No negative numbers");
                                    break;
                                }

                                else{
                                    StringBuilder descript = getDesc();
                                    System.out.println("\nAuction ID = #" + auction.createAuction(strtPrice, rsrvPrice, descript, uID));
                                    repeat = false;
                                }
                            }
                            break;

                        case 2:
                            util.printScreen("What's the Auction ID");
                            auctionID = scan.next();

                            if (auction.isOwner(auctionID, uID)) {
                                if (auction.findAuction(auctionID)) {
                                    System.out.println("Details for: #" + auctionID);
                                    System.out.println(auction.viewDetails(auctionID));
                                    System.out.println("The Current Bid is: £"+ auction.viewCurrentBid(auctionID));
                                }
                                else{
                                    System.out.println("That Auction ID could not be found");
                                }
                            }
                            else{
                                System.out.println("You can't view details for this auction because you are not the owner");
                            }

                            break;

                        case 3:
                            util.printScreen("What's the Auction ID");
                            auctionID = scan.next();

                            if (auction.isOwner(auctionID,uID)) {
                                if (auction.findAuction(auctionID)) {
                                    System.out.println("Deleting Auction : #" + auctionID);
                                    String winner = auction.getWinner(auctionID);
                                    if (auction.reserveAchieved(auctionID)) {
                                        System.out.println("Winner username:" + winner);
                                        System.out.println(auction.viewWinnerDetails(winner));
                                    }
                                    else{
                                        System.out.println("Reserve Wasn't Achieved");
                                    }
                                    auction.deleteAuction(auctionID);
                                    System.out.println("Auction Deleted");
                                }
                                else{
                                    System.out.println("That Auction ID could not be found");
                                }
                            }

                            else{
                                System.out.println("Auction couldn't be deleted because you are not the owner");
                            }
                            break;

                        case 4:
                            System.out.println("Username: " + uID);
                            break;

                        default :
                            System.out.println("\nThe option are: \n'1' for New Auction \n'2' for Viewing Auction Details \n'3' to Close an Auction \n");
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
                    serverAuth();
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