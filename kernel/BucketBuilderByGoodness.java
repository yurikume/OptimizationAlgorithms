package kernel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BucketBuilderByGoodness implements BucketBuilder{

	@Override
	public List<Bucket> build(List<Item> items, Configuration config, List<Item> y_ker) {
	
		// We select only the first few items in each family 
		// It's used in the first phase of the two-phase algorithm
		
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();

		List<Item> y_items = items.stream().filter(p -> p.getName().startsWith("y") && !y_ker.contains(p)).collect(Collectors.toList());
		
		// Number of items to take into consideration for each family
		int items_limit = config.getItemsLimit(); 

		int size = (int) Math.floor(y_items.size() * items_limit * config.getBucketSize());
		List<Item> x_list;
		
		for(Item it : y_items) {
			b.addItem(it);
			
			String vars[]= it.getName().split("_");
            String fam = vars[1];
            String knap = vars[2];
            
			x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			
			for(int i = 0; i < items_limit; i++) {
				b.addItem(x_list.get(i));
			}
			
			if(b.size() >= size) {
				buckets.add(b);
				b = new Bucket();
			}	
		}
		return buckets;
	}
}
