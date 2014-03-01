/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2013 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.util;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

/**
 * Une MultiMap qui permet de ne pas renvoyer <code>null</code>. De plus elle permet de choisir le
 * type de Collection utilisé.
 * 
 * @author ILM Informatique 8 sept. 2004
 * @param <K> type of the keys
 * @param <V> type of elements in collections
 */
@SuppressWarnings("unchecked")
public class CollectionMap<K, V> extends MultiHashMap {

    private static final int DEFAULT_CAPACITY = 16;

    /**
     * Create a map with a single entry.
     * 
     * @param <K> type of key.
     * @param <V> type of items.
     * @param key the single key.
     * @param values the values for <code>key</code>.
     * @return a map with one entry.
     */
    public static <K, V> CollectionMap<K, V> singleton(K key, Collection<V> values) {
        final CollectionMap<K, V> res = new CollectionMap<K, V>();
        res.putAll(key, values);
        return res;
    }

    public static <K, V> CollectionMap<K, V> singleton(K key, V... values) {
        return singleton(key, asList(values));
    }

    // to avoid
    // "Type safety : A generic array of Tuple2<String,Boolean> is created for a varargs parameter"
    public static <K, V> CollectionMap<K, V> singleton(K key, V value) {
        return singleton(key, Collections.singleton(value));
    }

    private final Class<? extends Collection<V>> collectionClass;
    private final Collection<V> collectionSpecimen;

    /**
     * Une nouvelle map avec ArrayList comme collection.
     */
    public CollectionMap() {
        this(ArrayList.class);
    }

    /**
     * Une nouvelle map. <code>collectionClass</code> doit descendre de Collection, et posséder un
     * constructeur prenant une Collection (c'est le cas de la majorité des classes de java.util).
     * 
     * @param aCollectionClass le type de collection utilisé.
     */
    public CollectionMap(Class aCollectionClass) {
        this(aCollectionClass, DEFAULT_CAPACITY);
    }

    /**
     * Une nouvelle map sans préciser le type de collection. Dans ce cas si vous voulez spécifier
     * une collection surchargez {@link #createCollection(Collection)}. Ce constructeur est donc
     * utile pour des raisons de performances (évite la réflexion nécessaire avec les autres).
     * 
     * @param initialCapacity the initial capacity.
     */
    public CollectionMap(final int initialCapacity) {
        this((Class) null, initialCapacity);
    }

    public CollectionMap(Class aCollectionClass, final int initialCapacity) {
        super(initialCapacity);
        this.collectionClass = aCollectionClass;
        this.collectionSpecimen = null;
    }

    public CollectionMap(Collection<V> collectionSpecimen) {
        this(collectionSpecimen, 16);
    }

    /**
     * A map that creates new collections by cloning collectionSpecimen. Allow one to customize an
     * instance, contrary to the constructor which only takes a class.
     * 
     * @param collectionSpecimen the collection from which to all others will be cloned.
     * @param initialCapacity the initial capacity
     * @throws IllegalArgumentException is not a Cloneable.
     */
    public CollectionMap(Collection<V> collectionSpecimen, final int initialCapacity) {
        super(initialCapacity);
        this.collectionClass = null;
        if (!(collectionSpecimen instanceof Cloneable))
            throw new IllegalArgumentException(collectionSpecimen + " not a cloneable.");
        // allow to pass an existing collection w/o us modifying it
        // plus test if copy() succeeds
        this.collectionSpecimen = CopyUtils.copy(collectionSpecimen);
        this.collectionSpecimen.clear();
    }

    /**
     * Renvoie la collection associée à la clef passée. Si la clef n'existe pas, renvoie une
     * collection vide.
     * 
     * @param key la clef.
     * @return le collectionClass (par défaut ArrayList) associé à la clef passée.
     * @see #getCollectionClass()
     */
    public Collection<V> getNonNull(K key) {
        final Collection<V> res = getNull(key);
        return res == null ? this.createCollection(res) : res;
    }

    /**
     * Just for the generics.
     * 
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains
     *         no mapping for the key.
     */
    public Collection<V> getNull(K key) {
        return (Collection<V>) this.get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.MultiHashMap#createCollection(java.util.Collection)
     */
    public Collection<V> createCollection(Collection coll) {
        if (this.collectionClass != null)
            try {
                if (coll == null) {
                    return this.collectionClass.newInstance();
                } else {
                    return this.collectionClass.getConstructor(new Class[] { Collection.class }).newInstance(new Object[] { coll });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else if (this.collectionSpecimen != null) {
            try {
                final Collection<V> res = CopyUtils.copy(this.collectionSpecimen);
                if (coll != null)
                    res.addAll(coll);
                return res;
            } catch (Exception e) {
                throw ExceptionUtils.createExn(IllegalStateException.class, "clone() failed", e);
            }
        } else
            return super.createCollection(coll);
    }

    public Class getCollectionClass() {
        return this.collectionClass;
    }

    public void removeAll(CollectionMap<? extends K, ? extends V> mm) {
        for (final Map.Entry<? extends K, ?> e : mm.entrySet()) {
            final Collection<V> coll = this.getNull(e.getKey());
            if (coll != null) {
                coll.removeAll((Collection<?>) e.getValue());
                // see #remove(Object, Object)
                if (coll.isEmpty())
                    this.remove(e.getKey());
            }
        }
    }

    public void removeAll(Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.remove(e.getKey(), e.getValue());
        }
    }

    /**
     * Fusionne la MultiMap avec celle-ci. C'est à dire rajoute les valeurs de mm à la suite des
     * valeurs de cette map (contrairement à putAll(Map) qui ajoute les valeurs de mm en tant que
     * valeur scalaire et non en tant que collection).
     * 
     * @param mm la MultiMap à fusionner.
     */
    public void merge(MultiMap mm) {
        // copied from super ctor
        for (Iterator it = mm.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            Collection<V> coll = (Collection<V>) entry.getValue();
            Collection newColl = createCollection(coll);
            this.putAll(entry.getKey(), newColl);
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map. This method is equivalent to
     * {@link MultiHashMap#MultiHashMap(Map)}. NOTE: cannot use Map<? extends K, ? extends V> since
     * java complains (MultiHashMap not being generic).
     * 
     * @param mapToCopy mappings to be stored in this map
     */
    @Override
    public void putAll(Map mapToCopy) {
        if (mapToCopy instanceof MultiMap) {
            this.merge((MultiMap) mapToCopy);
        } else {
            super.putAll(mapToCopy);
        }
    }

    public boolean putAll(K key, V... values) {
        return this.putAll(key, asList(values));
    }

    // generics : MultiHashMap is not generic but it extends HashMap who does
    // so just override

    @Override
    public Set<Map.Entry<K, Collection<V>>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public Collection<V> remove(Object key) {
        return (Collection<V>) super.remove(key);
    }
}