package kernel;
public class Item
{
	private String name;
	private double rc;
	private double xr;
	private int profit;
	private int weight;
	private double goodness;
	
	public Item(String name, double xr, double rc, int profit, int weight)
	{
		this.name = name;
		this.xr = xr;
		this.rc = rc;
		this.profit = profit;
		this.weight = weight;
		this.goodness = (profit-weight)*100.0/weight;
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getRc()
	{
		return rc;
	}
	
	public double getXr()
	{
		return xr;
	}
	
	public double getAbsoluteRC()
	{
		return Math.abs(rc);
	}

	public int getProfit() {
		return profit;
	}

	public int getWeight() {
		return weight;
	}

	public double getGoodness() {
		return goodness;
	}
	
}