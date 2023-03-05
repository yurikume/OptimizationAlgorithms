package kernel;
import java.util.ArrayList;
import java.util.List;

public class DefaultBucketBuilder implements BucketBuilder
{
	@Override
	public List<Bucket> build(List<Item> items, Configuration config, List<Item> y_ker)
	{
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		for(Item it : items)
		{
			b.addItem(it);
			
			if(b.size() == size)
			{
				buckets.add(b);
				b = new Bucket();
			}
		}
		if(b.size() < size && b.size() > 0)
		{
			buckets.add(b);
		}
		return buckets;
	}
}
