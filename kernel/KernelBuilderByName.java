package kernel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KernelBuilderByName implements KernelBuilder
{
	// I put in the kernel all the y with xr > 0 and their x (since they're ordered by name)
	// Nuova idea: mettere non tutte le x, ma solo le prime tot per inserire solo le più promettenti di ogni famiglia
	// In questo modo si dà la stessa opportunità a tutte le famiglie
	@Override
	public Kernel build(List<Item> items, Configuration config)
	{
		Kernel kernel = new Kernel();
		
		for(Item it : items)
		{
			if(it.getXr()> 0)
			{
				if(it.getName().startsWith("y")) {
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
			}
		}
		return kernel;
	}
}
