package kernel;
import java.util.ArrayList;
import java.util.List;

public class OverlappingBucketBuilder implements BucketBuilder
{
	@Override
	public List<Bucket> build(List<Item> items, Configuration config,List<Item> y_ker)
	{
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		
		System.out.println("size = " + size);
		
		for(int i = 0; i < items.size(); i ++) {
			b.addItem(items.get(i));
			
			
			if(b.size() == size) 
			{
				buckets.add(b);
				b = new Bucket();
					
				i = i - (int) Math.floor(size/100*50);
			}
		}
		if(b.size() < size && b.size() > 0)
		{
			buckets.add(b);
		}
		return buckets;
	}
}
