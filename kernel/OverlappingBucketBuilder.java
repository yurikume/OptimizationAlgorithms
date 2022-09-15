package kernel;
import java.util.ArrayList;
import java.util.List;

public class OverlappingBucketBuilder implements BucketBuilder
{
	@Override
	public List<Bucket> build(List<Item> items, Configuration config)
	{
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		//int size_first_bucket = (int) Math.floor(1.5 * items.size()*config.getKernelSize());
		
		System.out.println("size = " + size);
		//System.out.println("size_first_bucket = " + size_first_bucket);
		
		int num_buckets = 1;
		for(int i = 0; i < items.size(); i ++) {
			b.addItem(items.get(i));
			
			/*
			if(num_buckets != 1) 
			{
				if(b.size() == size)
				{
					buckets.add(b);
					b = new Bucket();
					
					i = i - (int) Math.floor(size/100*50);
					num_buckets++;
				}
			}
			else
			{*/
				if(b.size() == size) 
				{
					buckets.add(b);
					b = new Bucket();
					
					i = i - (int) Math.floor(size/100*50);
					num_buckets++;
				}
			//}
		}
		if(b.size() < size && b.size() > 0)
		{
			buckets.add(b);
		}
		return buckets;
	}
}
