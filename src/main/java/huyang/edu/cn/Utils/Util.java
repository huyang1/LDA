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

    /**
     * 概率抽样
     * @param p 概率密度数组
     * @return
     */
    public static int sample(double[] p) {

        int topic;
        for (int k = 1; k < p.length; ++k) {
            p[k] += p[k-1];
        }

        double u = Math.random() * p[p.length-1];
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic])
                break;
        }
        return topic;
    }
}
