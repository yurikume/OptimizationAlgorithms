package kernel;
import java.util.List;

public class KernelBuilderPercentage implements KernelBuilder
{
	@Override
	public Kernel build(List<Item> items, Configuration config)
	{
		Kernel kernel = new Kernel();
		
		for(Item it : items)
		{
			if(kernel.size() < Math.round(config.getKernelSize()*items.size()))
			{
				kernel.addItem(it);
			}
		}	
		return kernel;
	}
}