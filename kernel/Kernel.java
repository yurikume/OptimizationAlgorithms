package kernel;
import java.util.ArrayList;
import java.util.List;

public class Kernel
{
	private List<Item> items;
	
	public Kernel(List<Item> items)
	{
		this.items = items;
	}
	
	public Kernel()
	{
		this.items = new ArrayList<>();
	}
	
	public void addItem(Item it)
	{
		items.add(it);
	}
	
	public boolean contains(Item it)
	{
		return items.stream().anyMatch(it2 -> it2.getName().equals(it.getName()));
	}
	
	public int size()
	{
		return items.size();
	}
}