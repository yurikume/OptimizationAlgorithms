package kernel;
import java.util.Comparator;
import java.util.List;

public class ItemSorterByValueAndAbsoluteRC implements ItemSorter
{
	@Override
	public void sort(List<Item> items)
	{
		items.sort(Comparator.comparing(Item::getXr).reversed()
				.thenComparing(Item::getRc));
		
	}

}
