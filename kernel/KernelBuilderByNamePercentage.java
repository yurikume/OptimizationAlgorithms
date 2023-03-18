package kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KernelBuilderByNamePercentage implements KernelBuilder {

	// Come il by name ma prende solo una percentuale delle famiglie e non tutte quelle positive
	@Override
	public Kernel build(List<Item> items, Configuration config) {
		Kernel kernel = new Kernel();
		List<Item> y_items = items.stream().filter(it -> it.getName().startsWith("y")).collect(Collectors.toList());
		int y_da_prendere = (int)Math.ceil(y_items.size() * config.getKernelSize()); // so it means i take a percentage of y variables to put in the bucket
		
		for(int i = 0; i < y_da_prendere; i++) {
			Item it = y_items.get(i);
			kernel.addItem(it);
			
			String vars[]= it.getName().split("_");
	        String fam = vars[1];
	        String knap = vars[2];
	        
	        List<Item> x_list = new ArrayList<Item>();
			x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			for(Item x_item: x_list) {
				kernel.addItem(x_item);
			}	
		}
		return kernel;
	}

}
