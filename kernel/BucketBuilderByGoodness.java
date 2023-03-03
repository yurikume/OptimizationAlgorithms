package kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BucketBuilderByGoodness implements BucketBuilder {

	@Override
	public List<Bucket> build(List<Item> items, Configuration config) {
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		HashMap<Item,List<Item>> mappa = new HashMap<Item,List<Item>>();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		List<Item> y_items=items.stream().filter(p -> p.getName().startsWith("y")).collect(Collectors.toList());
		List<Item> x_items=items.stream().filter(p -> p.getName().startsWith("x")).collect(Collectors.toList());
		int limit_items = 10; // Il numero di items da prendere per ogni famiglia (sarà variabile)
		int index;
		List<Item> x_list;
		Item x_item;
		
		// Riempio la hashmap
		for(Item it : y_items) {
			String vars[]= it.getName().split("_");
            String fam = vars[1];
            String knap = vars[2];
			
			x_list = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			mappa.put(it, x_list);
		}
		
		// Ciclo su tutte le chiavi(le y) della hashmap e per ognuna prendo 10 x e le rimuovo
		// Finisco quando x_items è vuoto, cioè quando ho messo tutte le x in tutti i bucket
		while(!x_items.isEmpty()) {
			for(Item it : mappa.keySet()) {
				b.addItem(it); // Qui dovrei controllare se la y c'è già nella soluzione perchè altrimenti la inserisco più volte
				for(int i = 0; i < limit_items; i++) {
					if(mappa.get(it).isEmpty()) { // Se è vuota vuol dire che per quella y ho già inserito tutte le x
						break;
					}else {
						x_item = mappa.get(it).get(0);
						b.addItem(x_item);
						mappa.get(it).remove(0);
						x_items.remove(it); // Lo rimuovo anche da qui
					}
				}
				if(b.size() >= size) { // Controllo solo alla fine se cambiare bucket
					buckets.add(b);
					b = new Bucket();
				}
			}
		}
		
		return buckets;
	}

}
