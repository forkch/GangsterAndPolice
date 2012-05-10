package zuehlke.jic;

import java.util.concurrent.LinkedBlockingDeque;

public class AutoDiscardingDeque<E> extends LinkedBlockingDeque<E> {

    public AutoDiscardingDeque() {
        super();
    }

    public AutoDiscardingDeque(int capacity) {
        super(capacity);
    }

    @Override
    public synchronized boolean offerFirst(E e) {
        if (remainingCapacity() == 0) {
            removeLast();
        }
       return super.offerFirst(e);
    }
}