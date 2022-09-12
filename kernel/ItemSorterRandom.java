package kernel;

import java.util.Collections;
import java.util.List;

public class ItemSorterRandom implements ItemSorter {

	@Override
	public void sort(List<Item> items) {
		Collections.shuffle(items);
	}

}
