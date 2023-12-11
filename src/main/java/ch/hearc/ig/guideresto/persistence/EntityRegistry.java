package ch.hearc.ig.guideresto.persistence;

import java.util.*;

public class EntityRegistry<T> {

    protected final Map<Integer, T> identityMap = new HashMap<Integer, T>();

    public Optional<T> get(Integer id) {
        return this.identityMap.containsKey(id)
            ? Optional.of(this.identityMap.get(id))
            : Optional.empty();
    }

    public void set(Integer id, T entity) {
        if (entity == null) {
            this.identityMap.remove(id);
        } else {
            this.identityMap.put(id, entity);
        }
    }

    public void delete(Integer id) {
        this.identityMap.remove(id);
    }
}
