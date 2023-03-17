package kernel;

import java.util.List;

public class BucketBuilderByFirstItems implements BucketBuilder{

	@Override
	public List<Bucket> build(List<Item> items, Configuration config, List<Item> y_ker) {
		// Come il by goodness ma devo selezionare solamente i primi items di ogni famiglia (ex prima metà)
		// Usato nella prima iterazione del nuovo algoritmo
		// Nella seconda iterazione si può riciclare il builder by name
		
		return null;
	}

}
