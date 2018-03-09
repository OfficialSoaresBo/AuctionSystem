import java.text.*;
import java.util.*;

public class Utilities {
	
	public Utilities(){
		/*
		Utilities class constructor
		 */
	}

	/**
     * Email Validation
     * @param  email [description]
     * @return       [description]
     */
    public boolean validEmail(String email) {
        boolean valid = false;

        //checks for abc@something.com
        if (email.matches("[a-z0-9._%+-][a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{3}"))
            valid = true;

        //checks for abc@something.co.uk
        else if (email.matches("[a-z0-9._%+-][a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2}+\\.[a-z]{2}"))
            valid = true;

        //checks for abc@something.com with capitals
        else if (email.matches("[A-z0-9._%+-][A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{3}"))
            valid = true;

        //checks for abc@something.com with capitals
        else if (email.matches("[A-z0-9._%+-][A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2}+\\.[A-Z]{2}"))
            valid = true;

        return valid;
    }

    /**
     * Prints the parameter whilst adding padding
     * @param str [description]
     */
    public void printScreen(String str){
        System.out.println("\n***********************");
        System.out.println(str);
        System.out.println("***********************");
    }

    /**
     * Checks if number is within boundaries of 1 and 1000000
     * @param  num [description]
     * @return     [description]
     */
    public boolean validNum(double num){
        boolean res = false;

        if (num > 0 && num < 1000000)
            res = true;
        return res;
    }

    /**
     * Formats integer passed and returns a string formatted 
     * to 2 decimal points and with the currency symbol
     * @param  num [number to be formated]
     * @return     [string returned in the format of £num]
     */
    public String currencyFormat(double num){
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String moneyString = formatter.format(num);
        return moneyString;
    }
    /**
     * generates random auction 4 digit ID
     * @return [description]
     */
    public String genRandID()
        throws java.rmi.RemoteException{
        Random rand = new Random();
        String randId = String.format("%04d", rand.nextInt(10000));
        return randId;
    }
}