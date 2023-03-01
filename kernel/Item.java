package kernel;
public class Item
{
	private String name;
	private double rc;
	private double xr;
	private int c;
	private int a;
	private double goodness;
	
	public Item(String name, double xr, double rc, int c, int a)
	{
		this.name = name;
		this.xr = xr;
		this.rc = rc;
		this.c = c;
		this.a = a;
		this.goodness = (c-a)*100.0/a;
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

	public int getC() {
		return c;
	}

	public int getA() {
		return a;
	}

	public double getGoodness() {
		return goodness;
	}
	
}