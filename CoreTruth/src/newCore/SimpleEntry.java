package newCore;


/*Code copied from : http://stackoverflow.com/questions/3110547/java-how-to-create-new-entry-key-value*/
import java.util.Map;

final class SimpleEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public SimpleEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}
