package huyang.edu.cn.Utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Util<K,V> {

    public  K getKey(Map<K,V> map,V value) {

        Set set = map.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<K,V> entry = (Map.Entry) it.next();
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
