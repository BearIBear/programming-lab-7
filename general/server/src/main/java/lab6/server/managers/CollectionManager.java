package lab6.server.managers;

import java.util.ArrayList;
import java.util.PriorityQueue;

import lab6.models.MusicBand;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Менеджер коллекции для управления элементами типа MusicBand
 *
 * @author Михаил
 */
public class CollectionManager {
    private PriorityQueue<MusicBand> collection;
    private LocalDateTime initTime;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static long nextId = 1;
    private static ArrayList<Long> vacantIds = new ArrayList<>();

    public CollectionManager() {
        this.collection = new PriorityQueue<>();
        this.initTime = LocalDateTime.now(); 
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public PriorityQueue<MusicBand> getCollection() {
        return collection;
    }

    public LocalDateTime getInitTime() {
        return initTime;
    }    

    public void addElement(MusicBand band) {
        CollectionManager.fixId(band);
        collection.add(band);
    }

    public void addLoadedElement(MusicBand band) {
        collection.add(band);
    }

    public void clearCollection() {
        collection.clear();
    }

    public boolean removeElement(long id) {
        MusicBand bandToRemove = collection.stream().filter(band -> band.getId() == id).toArray(MusicBand[]::new)[0];
        return collection.remove(bandToRemove);
    }

    public static void setNextId(long nextId) {
        CollectionManager.nextId = nextId;
    }

    public static long getNextId() {
        return nextId;
    }

    public static void fixId(MusicBand band) {
        if (vacantIds.isEmpty()) {
            band.setId(nextId++);
        } else {
            band.setId(vacantIds.remove(0));
        }
    }

    public static ArrayList<Long> getVacantIds() {
        return vacantIds;
    }

    public static void setVacantIds(ArrayList<Long> vacantIds) {
        CollectionManager.vacantIds = vacantIds;
    }

    public static void addVacantId(Long vacantId) {
        CollectionManager.vacantIds.add(vacantId);
    }
}