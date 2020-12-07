package elkaproj.kvcreader;

import java.lang.annotation.*;

/**
 * Indicates the serialized name of a field in a Key-Value Configuration stream.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface KVCName {

    /**
     * Indicates the serialized name of the field.
     * @return The serialized name of the field.
     */
    String name() default "";
}
