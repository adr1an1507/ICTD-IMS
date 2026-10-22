/*
 * ICTD IMS - HASH TABLE
 *
 * A simple hash table made from scratch (no java.util.HashMap).
 * It stores values by a String key, for example:
 *   key = username (lowercase), value = Account
 *
 * How it works:
 *   - The key is turned into a number (index) to pick a "bucket".
 *   - Each bucket is a linked list, so keys with the same index can share it
 *     (this is called chaining).
 *   - When the table gets too full, it doubles in size so it stays fast.
 *
 * Speed: put, get, remove and containsKey are O(1) on average.
 */
package backend.auth;

import java.util.ArrayList;
import java.util.List;

public class HashTable<V> {

    // One item in a bucket's linked list
    private static class Node<V> {
        final String key;
        V value;
        Node<V> next;

        Node(String key, V value, Node<V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final int START_SIZE = 16;
    private static final double MAX_LOAD = 0.75;

    private Node<V>[] buckets;
    private int size = 0;

    public HashTable() {
        buckets = newBuckets(START_SIZE);
    }

    @SuppressWarnings("unchecked")
    private Node<V>[] newBuckets(int length) {
        return (Node<V>[]) new Node[length];
    }

    // Turns a key into a bucket number
    private int indexFor(String key, int length) {
        return (key.hashCode() & 0x7fffffff) % length;
    }

    /** Adds a value. If the key already exists, its value is replaced. */
    public void put(String key, V value) {
        int index = indexFor(key, buckets.length);

        for (Node<V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }

        buckets[index] = new Node<>(key, value, buckets[index]);
        size++;

        if (size > buckets.length * MAX_LOAD) {
            resize();
        }
    }

    /** Returns the value for the key, or null if the key is not found. */
    public V get(String key) {
        int index = indexFor(key, buckets.length);

        for (Node<V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    /** Returns true if the key exists. */
    public boolean containsKey(String key) {
        int index = indexFor(key, buckets.length);

        for (Node<V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /** Removes the key. Returns the removed value, or null if not found. */
    public V remove(String key) {
        int index = indexFor(key, buckets.length);
        Node<V> previous = null;

        for (Node<V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                if (previous == null) {
                    buckets[index] = node.next;
                } else {
                    previous.next = node.next;
                }
                size--;
                return node.value;
            }
            previous = node;
        }
        return null;
    }

    /** Returns all values in a new list (the order is not guaranteed). */
    public List<V> values() {
        List<V> list = new ArrayList<>();

        for (Node<V> head : buckets) {
            for (Node<V> node = head; node != null; node = node.next) {
                list.add(node.value);
            }
        }
        return list;
    }

    /** Returns how many items are stored. */
    public int size() {
        return size;
    }

    // Doubles the table size and puts every item in its new bucket
    private void resize() {
        Node<V>[] oldBuckets = buckets;
        buckets = newBuckets(oldBuckets.length * 2);

        for (Node<V> head : oldBuckets) {
            for (Node<V> node = head; node != null; node = node.next) {
                int index = indexFor(node.key, buckets.length);
                buckets[index] = new Node<>(node.key, node.value, buckets[index]);
            }
        }
    }
}