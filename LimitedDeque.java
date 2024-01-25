package tinker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class LimitedDeque implements Iterable<Experience> {
    private final int maxSize;
    private final Deque<Experience> deque;

    public LimitedDeque(int maxSize) {
        this.maxSize = maxSize;
        this.deque = new ArrayDeque<>();
    }    

    public int size() {
        return deque.size();
    }

    public void add(Experience value) {
        deque.addLast(value);

        if (deque.size() > maxSize) {
            deque.removeFirst();
        }
    }

    public void addAll(List<Experience> experiences) {
        for (Experience experience : experiences) {
            add(experience);
            if (size() >= maxSize) {
                break;
            }
        }
    }

    public List<Experience> getRandomSample(int batchSize) {
        List<Experience> memoryList = new ArrayList<>(deque);

        Collections.shuffle(memoryList);

        int endIndex = Math.min(batchSize, memoryList.size());
        List<Experience> miniSample = memoryList.subList(0, endIndex);

        return miniSample;
    }

    public List<Experience> getAllExperiences() {
        return new ArrayList<>(deque);
    }

    @Override
    public Iterator<Experience> iterator() {
        return deque.iterator();
    }
}
