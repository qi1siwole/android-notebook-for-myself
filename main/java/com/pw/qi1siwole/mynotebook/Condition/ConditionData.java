package com.pw.qi1siwole.mynotebook.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by user on 2017/3/30.
 */

public class ConditionData<K, V> {

    private List<K> mKeys = null;
    //private Set<K> mKeys = null;
    //private Map<K, Set<V>> mMap = null;

    private RequestDataMethod<K, V> mRequestDataMethod;

    public ConditionData(RequestDataMethod<K, V> requestDataMethod) {
        mRequestDataMethod = requestDataMethod;

        mKeys = new ArrayList<>();
        //mKeys = new HashSet<>();
        //mMap = new HashMap<>();
    }

    /*
     * 设置Key集合
     */
    /*
    public void setKeys(Collection<K> keys) {
        if (!mKeys.isEmpty()) {
            mKeys.clear();
        }
        mKeys.addAll(keys);
    }
    */

    /*
    public void addKey(K key) {
        if (!mKeys.contains(key)) {
            mKeys.add(key);
        }
    }
    */

    /*
     * 获取Key集合
     */
    public List<K> getKeys() {
        return mKeys;
    }

    /*
    public void setValue(K key, Collection<V> value) {
        addKey(key);
        Set<V> set = new HashSet<>();
        set.addAll(value);
        mMap.put(key, set);
    }

    public Set<V> getValue(K key) {
        return mMap.get(key);
    }
    */

    /*
     * 请求获取Key集合
     */
    public List<K> requestKeys() {
        mKeys = mRequestDataMethod.requestKeys();
        return mKeys;
    }

    /*
     * 请求获得Key对于的Value
     */
    public Set<V> requestValue(K key) {
        return mRequestDataMethod.requestValue(key);
    }

    interface RequestDataMethod<K, V> {
        List<K> requestKeys();
        Set<V> requestValue(K key);
    }
}
