package javax.annotation;

import java.lang.annotation.Documented;


/**
 * Created this annotation to provide a definition for the @Nullable annotation from GWT to circumvent
 * a Scala compiler error, when it tries to analyze the definition of com.google.common.Optional
 *
 * Created by Markus Ackermann.
 * No rights reserved.
 */

@Documented
public @interface Nullable {

}
