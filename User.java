import java.util.*;

public class User{

	public String name, email, uID;
	public HashMap<String, Double> currentBids = new HashMap<String, Double>();

	public User(String name, String email, String uID){
		this.name = name;
		this.email = email;
		this.uID = uID;
	}

	/**
	 * gets buyer name
	 * @return [description]
	 */
	public String getName(){
		return name;
	}

	/**
	 * gets buyer email
	 * @return [description]
	 */
	public String getEmail(){
		return email;
	}

	/**
	 * Adds new bid to the hasmap
	 * @param  aucID      [description]
	 * @param  bidAmmount [description]
	 * @return            [description]
	 */
	public boolean regBid(String aucID, double bidAmmount){
		currentBids.put(aucID,bidAmmount);
		return true;
	}

	/**
	 * Get bid made by user
	 * @param  aucID [description]
	 * @return       [description]
	 */
	public Double getBidMade(String aucID){
		return currentBids.get(aucID);
	}
}