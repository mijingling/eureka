package com.netflix.eureka2.registry;

import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.interests.SourcedChangeNotification;
import rx.Observable;

import java.util.Collection;

/**
 * A holder object that maintains copies of the same data (as defined by some metric, such as id) that are
 * from different sources. Updates/removes to this holder are only done to the copy from the corresponding source.
 *
 * The holder should maintain a consistent view (w.r.t. to it's view, but not necessarily to a particular copy)
 * of this data to return.
 *
 * @param <V> the data type
 *
 * @author David Liu
 */
public interface MultiSourcedDataHolder<V> {

    /**
     * @return the uuid that is common to all the data copies
     */
    String getId();

    /**
     * @return the number of copies of data currently in this holder
     */
    int size();

    /**
     * @return the view copy of the data, if exists
     */
    V get();

    /**
     * @return the copy of data for the given source, if exists
     */
    V get(Source source);

    /**
     * @return the source of the view copy of the data, if exists
     */
    Source getSource();

    /**
     * @return a collection of all the sources that have copies in this holder
     */
    Collection<Source> getAllSources();

    /**
     * @return the view copy of the data as a change notification, if exists
     */
    SourcedChangeNotification<V> getChangeNotification();

    /**
     * @param source the source to update
     * @param data the data copy
     */
    Observable<Status> update(Source source, V data);

    /**
     * @param source the source to delete
     * @param data the specific data copy for the given source to delete subject to versioning
     */
    Observable<Status> remove(Source source, V data);

    /**
     * @param source the source to delete
     */
    Observable<Status> remove(Source source);

    public enum Status {
        AddedFirst,      // Add result of the first add operation to a new (empty) holder
        AddedChange,     // Add result that modifies an existing copy in the holder
        AddExpired,      // Add result of a lower version copy. This is a no-op
        RemovedFragment, // Remove result that removes a copy in the holder
        RemovedLast,     // Remove result of the operation that removes the last copy in the holder
        RemoveExpired    // Remove result of a lower versioned or non-existent copy. This is a no-op
    }

    final class Snapshot<V> {
        private final SourcedChangeNotification<V> notification;

        protected Snapshot(Source source, V data) {
            this.notification = new SourcedChangeNotification<>(ChangeNotification.Kind.Add, data, source);
        }

        Source getSource() {
            return notification.getSource();
        }

        V getData() {
            return notification.getData();
        }

        SourcedChangeNotification<V> getNotification() {
            return notification;
        }
    }


    /**
     * An interface for an accessor to the data store that holds th
     */
    interface HolderStoreAccessor<E extends MultiSourcedDataHolder> {
        void add(E holder);
        E get(String id);
        void remove(String id);
        boolean contains(String id);
    }

}