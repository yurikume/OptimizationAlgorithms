package kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KernelBuilderByGoodness implements KernelBuilder {

	@Override
	public Kernel build(List<Item> items, Configuration config) {
		Kernel kernel = new Kernel();
		List<Item> y_items=items.stream().filter(p -> p.getName().startsWith("y")).collect(Collectors.toList());
		
		// number of items to take in consideration
		int items_limit = config.getItemsLimit();
		
		int size;
		if(config.getBucketBuilder() instanceof BucketBuilderByGoodness) {
			size = (int) Math.floor(y_items.size() * items_limit * config.getKernelSize());
		}else {
			size = (int) Math.floor(items.size()*config.getKernelSize());
		}
		
		for(Item it : y_items) {
			kernel.addItem(it);
			
			String vars[]= it.getName().split("_");
            String fam = vars[1];
            String knap = vars[2];
            
            List<Item> x_list = new ArrayList<Item>();
			x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			for(int i = 0; i < items_limit; i++) {
				kernel.addItem(x_list.get(i));
			}
			
			if(kernel.size() >= size) {
				break;
			}	
		}
		return kernel;
	}
}
