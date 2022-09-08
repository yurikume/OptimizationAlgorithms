package kernel;
import java.util.List;

public interface BucketBuilder
{
	public List<Bucket> build(List<Item> items, Configuration config);
}