package kernel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSorterByName implements ItemSorter
{
	@Override
	public void sort(List<Item> items)
	{
		items.sort(Comparator.comparing(Item::getXr).reversed()
				.thenComparing(Item::getRc));
		List<Item> y_items=items.stream().filter(p -> p.getName().startsWith("y")).collect(Collectors.toList());
		List<Item> fin_item = new ArrayList<Item>();
		List<Item> x_list = new ArrayList<Item>();
		for(Item y_it: y_items) {
			
			String vars[]= y_it.getName().split("_");
            String fam = vars[1];
            String knap = vars[2];
			
			x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			
			fin_item.add(y_it);
			// Riordino anche le x in base alla goodness
			x_list.sort(Comparator.comparing(Item::getXr).reversed()
					.thenComparing(Item::getGoodness).reversed());
			fin_item.addAll(x_list);
		}
		items.clear();
		items.addAll(fin_item);
	}

}
