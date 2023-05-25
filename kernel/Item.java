package kernel;

import java.util.Objects;

public class Item
{
	private String name;
	private double rc;
	private double xr;
	private int profit;
	private int weight;
	private double goodness;
	
	public Item(String name, double xr, double rc, int profit, int weight, double goodness)
	{
		this.name = name;
		this.xr = xr;
		this.rc = rc;
		this.profit = profit;
		this.weight = weight;
		this.goodness = goodness;
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
	
	public void setGoodness(double goodness) {
		this.goodness = goodness;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		return Objects.equals(name, other.name);
	}
}