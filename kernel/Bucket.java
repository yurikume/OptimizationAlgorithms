package kernel;
import java.util.ArrayList;
import java.util.List;

public class Bucket
{
	private List<Item> items;
	
	public Bucket()
	{
		items = new ArrayList<>();
	}
	
	public void addItem(Item it)
	{
		items.add(it);
	}
	
	public int size()
	{
		return items.size();
	}
	
	public List<Item> getItems()
	{
		return items;
	}
	
	public boolean contains(Item it)
	{
		return items.stream().anyMatch(it2 -> it2.getName().equals(it.getName()));
	}
	
	public void removeItem(Item it)
	{
		for(int i = 0; i< items.size(); i++)
		{
			Item it2 = items.get(i);
			if(it2.getName().equals(it.getName()))
			{
				items.remove(it2);
				break;
			}
		}
	}
	
	public void copy(List<Item> list) {
		for(Item it : list) {
			this.items.add(it);
		}
	}
	
	public void sortItems(ItemSorter sorter) {
		sorter.sort(this.items);
	}
}