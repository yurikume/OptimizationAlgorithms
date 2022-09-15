package kernel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PercentageBucketBuilder implements BucketBuilder
{
	@Override
	public List<Bucket> build(List<Item> items, Configuration config)
	{
		// usare sorting per nome
		
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		
		List<Item> y_items=items.stream().filter(p -> p.getName().startsWith("y")).collect(Collectors.toList());
		int y_da_prendere = (int) Math.floor(y_items.size()/(items.size()/size));
		
		while(y_items.size()>0) {
			System.out.println("y_items = " + y_items.size());
			
			List<Item> x_list = new ArrayList<Item>();
			
			for(int i=0; i<y_da_prendere; i++) {
				if(i < y_items.size()) {
					Item y_item = y_items.get(i);
					y_items.remove(i);
					b.addItem(y_item);
					
					String vars[]= y_item.getName().split("_");
		            String fam = vars[1];
		            String knap = vars[2];
					
					x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
					for(Item x_item: x_list) {
						b.addItem(x_item);
					}
				}	
			}
			
			buckets.add(b);
			b = new Bucket();
			
			for(int i=0; i<y_items.size(); i++) {
				Item y_item = y_items.get(i);
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
		}
		return buckets;
	}
}
