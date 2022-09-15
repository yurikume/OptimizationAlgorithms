package kernel;
import java.util.List;
import java.util.stream.Collectors;

public class KernelBuilderPositive implements KernelBuilder
{
	@Override
	public Kernel build(List<Item> items, Configuration config)
	{
		Kernel kernel = new Kernel();
		
		for(Item it : items)
		{
			if(it.getXr()> 0)
			{
				kernel.addItem(it);
			}
		}
		return kernel;
	}
}
