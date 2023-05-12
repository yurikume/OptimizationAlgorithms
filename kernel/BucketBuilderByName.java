package kernel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BucketBuilderByName implements BucketBuilder
{
	// Here we put a percentage of y items and their corresponding x into the same bucket
	@Override
	public List<Bucket> build(List<Item> items, Configuration config, List<Item> y_ker)  
	{
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		
		// We insert all the y that are not in the kernel in the y_items list
		List<Item> y_items=items.stream().filter(it -> it.getName().startsWith("y") && !y_ker.contains(it)).collect(Collectors.toList());
		
		// We take into consideration only a percentage of the y variables to insert into the buckets
		int y_da_prendere = (int)Math.ceil(y_items.size() * config.getBucketSize());
		int i = 0;
		int limit;
		
		while(y_items.size()>0) {
			System.out.println("y_items = " + y_items.size());
			
			List<Item> x_list = new ArrayList<Item>();
	
			if(y_da_prendere < y_items.size()) {
				limit = y_da_prendere;
			}else {
				limit = y_items.size();
			}
			
			for(i=0; i < limit; i++) {			
				Item y_item = y_items.get(0);
				y_items.remove(0);
				b.addItem(y_item);
				
				String vars[]= y_item.getName().split("_");
	            String fam = vars[1];
	            String knap = vars[2];
				
				x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
				for(Item x_item: x_list) {
					b.addItem(x_item);
				}
			}
			buckets.add(b);
			b = new Bucket();
		}
		return buckets;
	}
}
