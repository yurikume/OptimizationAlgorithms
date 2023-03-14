package kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BucketBuilderByGoodness implements BucketBuilder {

	@Override
	public List<Bucket> build(List<Item> items, Configuration config,List<Item> y_ker) {
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		LinkedHashMap<Item,List<Item>> mappa = new LinkedHashMap<Item,List<Item>>();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		List<Item> y_items=items.stream().filter(p -> p.getName().startsWith("y")).collect(Collectors.toList());
//		List<Item> x_items=items.stream().filter(p -> p.getName().startsWith("x")).collect(Collectors.toList());
		int items_limit = config.getItemsLimit(); // Il numero di items da prendere per ogni famiglia (sarà variabile)
		int index;
		List<Item> x_list;
		Item x_item;
		int count = 0;
		int x_it_size = items.size() - y_items.size();
		Boolean first_iter = true;
		
		// Riempio la hashmap 
		for(Item it : y_items) {
			String vars[]= it.getName().split("_");
            String fam = vars[1];
            String knap = vars[2];
			
			x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			mappa.put(it, x_list);
		}
		
		// Ciclo su tutte le chiavi(le y) della hashmap e per ognuna prendo items_limit x e le rimuovo
		while(count < x_it_size) {
			for(Item it : mappa.keySet()) {
				if(first_iter && y_ker.contains(it)) {
					continue;
				}
				
				b.addItem(it); // Qui dovrei controllare se la y c'è già nella soluzione perchè altrimenti la inserisco più volte
				
				// Il controllo non può essere fatto qui, deve essere fatto nel metodo di kernel search
				for(int i = 0; i < items_limit; i++) {
					if(mappa.get(it).isEmpty()) { // Se è vuota vuol dire che per quella y ho già inserito tutte le x
						break;
					}else {
						x_item = mappa.get(it).get(0);
						b.addItem(x_item);
						mappa.get(it).remove(0);
						count++;// Conta quanti item x vengono inseriti
					}
				}
				if(b.size() >= size) { // Controllo solo alla fine se cambiare bucket
					buckets.add(b);
					b = new Bucket();
				}
			}
			first_iter = false;
		}
		return buckets;
	}

}
