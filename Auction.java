public class Auction {

	private double startPrice, minPrice, currentPrice = 0;
	private StringBuilder desc = new StringBuilder();
    private String currentBidder, auctionOwner;

	/**
	 * Class constructor
	 * @param  startPrice [description]
	 * @param  minPrice   [description]
	 * @param  desc       [description]
	 * @return            [description]
	 */
	public Auction(double inpstartPrice, double inpminPrice, StringBuilder inpdesc, String auctionOwner){
		this.startPrice = inpstartPrice;
		this.minPrice = inpminPrice;
		this.desc = inpdesc;
		this.currentPrice = inpstartPrice;
        this.auctionOwner = auctionOwner;
	}

	/**
	 * Returns Starting Price
	 * @return [description]
	 */
	public double getStartPrice(){
	    return startPrice;
    }

    /**
     * Returns Minimum Price
     * @return [description]
     */
    public double getMinPrice(){
	    return minPrice;
    }

    /**
     * Returns Description
     * @return [description]
     */
    public StringBuilder getDesc(){
        return desc;
    }

    public String getOwner(){
        return auctionOwner;
    }

    /**
     * Returns Current Price
     * @return [description]
     */
    public double getCurrentPrice(){
    	return currentPrice;
    }

    /**
     * Returns Current Highest Bidder
     * @return [description]
     */
    public String getCurrentBider(){
        return currentBidder;
    }

    /**
     * Adds new Bid to the current auction
     * @param newBid [description]
     */
    public void addNewBid(double newBid, String bidder){
        currentBidder = bidder;
    	currentPrice = newBid;
    }
}