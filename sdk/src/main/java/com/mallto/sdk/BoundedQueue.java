package com.mallto.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 限制大小的队列
 * @param <T>
 */
public class BoundedQueue<T> {
    private final Deque<T> queue;
    private final int maxSize;

    public BoundedQueue(int maxSize) {
        this.queue = new ArrayDeque<>();
        this.maxSize = maxSize;
    }

    public void add(T item) {
        if (queue.size() >= maxSize) {
            queue.removeFirst(); // 移除最前面的元素
        }
        queue.addLast(item);
    }

    public T remove() {
        return queue.pollFirst(); // 移除并返回最前面的元素
    }

    public T peek() {
        return queue.peekFirst(); // 查看最前面的元素但不移除
    }

    public int size() {
        return queue.size(); // 当前队列大小
    }

    public boolean isEmpty() {
        return queue.isEmpty(); // 判断队列是否为空
    }

    public Deque<T> getDeque() {
        return queue;
    }

    public void clear() {
        queue.clear();
    }

    @NonNull
    @Override
    public String toString() {
        return "BoundedQueue{" +
                "queue=" + queue +
                ", maxSize=" + maxSize +
                '}';
    }
}
