package de.fusionfactory.index_vivus.models;

import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface ICrudOps<T> {

    /**
     * @return a copy of this with the id field updated to the generated auto-inc value from the database
     * @throws de.fusionfactory.index_vivus.persistence.ORMError
     */
    public T insertAsNew();

    /**
     * @return number of affected rows
     * @throws de.fusionfactory.index_vivus.persistence.ORMError
     */
    public int update();

    /**
     * @return number of affected rows
     * @throws de.fusionfactory.index_vivus.persistence.ORMError
     */
    public int delete();


    public List<T> duplicateList();
}
