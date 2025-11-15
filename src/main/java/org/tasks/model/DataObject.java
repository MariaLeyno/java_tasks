package org.tasks.model;

/**
 * Contract for data objects that can be stored into in-memory and files {@link org.tasks.storage.Storage}.
 */
public interface DataObject {

    /**
     * Method should return a string field or string combination of fields that uniquely identifies the data object.
     * @return unique identifier of data object
     */
    String getPrimary();

    /**
     * Method should return actual data object state: active or not.
     * @return is data object active
     */
    boolean isActive();

    /**
     * Method should change data object state to inactive.
     */
    void setInactive();
}
