
package webfootprint.engine.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DefaultUserData
{
    // maps a Key to a Pair( UserData, CopyAction )
    private Map userDataStorage;

    private Map getStorage() {
        if (userDataStorage == null) {
            userDataStorage = new HashMap();
        }
        return userDataStorage;
    }

    public Object clone() throws CloneNotSupportedException {
        DefaultUserData ud = (DefaultUserData) super.clone();
        ud.userDataStorage = null;
        return ud;
    }
 
    public void addUserDatum(Object key, Object value) {
        if (key == null)
            throw new IllegalArgumentException("Key must not be null");
        if (!getStorage().containsKey(key)) {
            getStorage().put(key, value);
        } else {
            throw new IllegalArgumentException("Key <" + key
                    + "> had already been added to an object with keys "
                    + getKeys());
        }
    }

    private Set getKeys() {
        return getStorage().keySet();
    }

    public void setUserDatum(Object key, Object value) {
        getStorage().put(key, value);
    }

    public Object getUserDatum(Object key) {
        Object value = getStorage().get(key);
        if (value == null) return null;
        return value;
    }

    public Object removeUserDatum(Object key) {
        Object o = getUserDatum(key);
        getStorage().remove(key);
        return o;
    }

    public Iterator getUserDatumKeyIterator() {
        return getStorage().keySet().iterator();
    }

    public boolean containsUserDatumKey(Object key)
    {
        return getStorage().containsKey(key);
    }

}
