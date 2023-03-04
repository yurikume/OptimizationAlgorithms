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
		// Perchè ordinato in questo modo? Dovrebbero esserci a parità di value prima
		// gli item con costo ridotto maggiore (o minore in modulo)
		// Questi non sono gli absolute RC, quindi non va bene
	}

}
