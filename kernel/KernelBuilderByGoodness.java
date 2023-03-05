package kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KernelBuilderByGoodness implements KernelBuilder {

	@Override
	public Kernel build(List<Item> items, Configuration config) {
		Kernel kernel = new Kernel();
		int size = (int) Math.floor(items.size()*config.getKernelSize());
//		List<Item> y_items=items.stream().filter(p -> p.getName().startsWith("y")).collect(Collectors.toList());
//		List<Item> x_items=items.stream().filter(p -> p.getName().startsWith("x")).collect(Collectors.toList());
		int limit_items = 15; // Il numero di items da prendere per ogni famiglia (sarà variabile)

		for(Item it : items) {
			if(it.getName().startsWith("y")) {
				kernel.addItem(it);
				
				String vars[]= it.getName().split("_");
	            String fam = vars[1];
	            String knap = vars[2];
	            
	            List<Item> x_list = new ArrayList<Item>();
				x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
				for(int i = 0; i < limit_items; i++) {
					kernel.addItem(x_list.get(i));
				}
			}
			if(kernel.size() >= size) {
				break;
			}	
		}
		return kernel;
	}

}
