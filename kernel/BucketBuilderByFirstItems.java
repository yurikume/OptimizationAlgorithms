package kernel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BucketBuilderByFirstItems implements BucketBuilder{

	@Override
	public List<Bucket> build(List<Item> items, Configuration config, List<Item> y_ker) {
		// Come il by goodness ma devo selezionare solamente i primi items di ogni famiglia (ex prima metà)
		// Usato nella prima iterazione del nuovo algoritmo
		// Nella seconda iterazione si può riciclare il builder by name
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		LinkedHashMap<Item,List<Item>> mappa = new LinkedHashMap<Item,List<Item>>();

		List<Item> y_items = items.stream().filter(p -> p.getName().startsWith("y") && !y_ker.contains(p)).collect(Collectors.toList());
		int items_limit = config.getItemsLimit(); // Il numero di items da prendere per ogni famiglia (sarà variabile)
//		int size = (int) Math.floor(items.size()*config.getBucketSize());
		// Qui uso solo parte degli items quindi la size è più piccola
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