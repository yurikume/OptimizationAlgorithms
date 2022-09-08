package kernel;
import java.util.HashMap;
import java.util.Map;

public class Solution
{
	private double obj;
	private Map<String, Double> vars;
	private boolean empty;
	
	public Solution()
	{
		empty = true;
		vars = new HashMap<>();
	}
	
	public double getObj()
	{
		return obj;
	}
	
	public double getVarValue(String name)
	{
		return vars.get(name);
	}

	public void setObj(double obj)
	{
		this.obj = obj;
	}

	public void setVars(Map<String, Double> vars)
	{
		this.vars = vars;
		empty = false;
	}

	public void setEmpty(boolean empty)
	{
		this.empty = empty;
	}

	public boolean isEmpty()
	{
		return empty;
	}
}
