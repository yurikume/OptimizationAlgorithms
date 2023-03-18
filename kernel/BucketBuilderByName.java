package kernel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BucketBuilderByName implements BucketBuilder
{
	// Also here we put a percentage of y items and their corresponding x in the same bucket
	@Override
	public List<Bucket> build(List<Item> items, Configuration config, List<Item> y_ker)  
	{
		// usare sorting per nome
		
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
//		int size = (int) Math.floor(items.size()*config.getBucketSize());
		
		// In y_items metto le y che non sono nel kernel
		List<Item> y_items=items.stream().filter(it -> it.getName().startsWith("y") && !y_ker.contains(it)).collect(Collectors.toList());
		int y_da_prendere = (int)Math.ceil(y_items.size() * config.getBucketSize());// so it means i take a percentage of y variables to put in the bucket
		int i = 0;
		int limit;
		
		while(y_items.size()>0) {
			System.out.println("y_items = " + y_items.size());
			
			List<Item> x_list = new ArrayList<Item>();
	
			if(y_da_prendere < y_items.size()) { // Se le y da prendere sono meno degli item rimasti ancora da aggiungere
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
