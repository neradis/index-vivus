package de.fusionfactory.index_vivus.models;

import scala.slick.session.Session;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public interface ICrudOpsProvider<T, O extends ICrudOps<T>> {

    public boolean contentsEqual(T other);

    public O crud();

    public O crud(Session tx);
}
