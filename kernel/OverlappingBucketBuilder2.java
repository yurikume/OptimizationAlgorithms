package kernel;
import java.util.ArrayList;
import java.util.List;

public class OverlappingBucketBuilder2 implements BucketBuilder
{
	@Override
	public List<Bucket> build(List<Item> items, Configuration config)
	{
		List<Bucket> buckets = new ArrayList<>();
		Bucket b = new Bucket();
		int size = (int) Math.floor(items.size()*config.getBucketSize());
		int size_first_bucket = (int) Math.floor(1.5 * items.size()*config.getKernelSize());
		
		System.out.println("size = " + size);
		System.out.println("size_first_bucket = " + size_first_bucket);
		
		for(int i=0; i<size_first_bucket; i++) {
			b.addItem(items.get(i));
			items.remove(i);
		}
		System.out.println("size b = " + b.size());
		buckets.add(b);
		
		int prev = (int) Math.floor(size/2);
		while(items.size()>size) {
			Bucket new_b = new Bucket();
			for(int j=0; j<size; j++) {
				if(j>=items.size()) break;
				
				if(j < prev) {
					Item it = b.getItems().get( b.getItems().size()-1-j );
					new_b.addItem(it);
				}else {
					new_b.addItem(items.get(j));
					items.remove(j);
				}
			}
			System.out.println("size b = " + new_b.size());
			buckets.add(new_b);
			b = new_b;
		}
		
		Bucket new_b = new Bucket();
		
		prev = (int) Math.floor(items.size()/2);
		for(int i=0; i<items.size(); i++) {
			if(i < prev) {
				Item it = b.getItems().get( b.getItems().size()-1-i );
				new_b.addItem(it);
			}else {
				new_b.addItem(items.get(i));
				items.remove(i);
			}
		}
		
		System.out.println("size b = " + new_b.size());
		buckets.add(new_b);
		
		return buckets;
	}
}
