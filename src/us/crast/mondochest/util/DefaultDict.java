package us.crast.mondochest.util;

import java.util.HashMap;
import java.util.List;

public class DefaultDict<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = -5131836669351168996L;
	ObjectMaker<V> builder;
	
	public DefaultDict(ObjectMaker<V> builder) {
		super();
		this.builder = builder;
	}
	/**
	 * Get an object at a key, creating a new one if one does not exist at this key.
	 * This is useful for mutable collections in a dictionary, to avoid boilerplate:
	 *     DefaultDict<String, ArrayList> somemap = new DefaultDict<String, ArrayList>();
	 *     somemap.ensure(key).add(foo);
	 */
	public V ensure(K key) {
		V item = this.get(key);
		if (item == null) {
			item = builder.build();
			this.put(key, item);
		}
		return item;
	}
}

class ListBuilder<T> implements ObjectMaker<List<T>> {
	@Override
	public List<T> build() {
		return new java.util.ArrayList<T>();
	}
}


