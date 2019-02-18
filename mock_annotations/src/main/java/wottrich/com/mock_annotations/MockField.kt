package wottrich.com.mock_annotations

import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * @author lucas.wottrich
 * @since 30/01/2019
 */

@Target(AnnotationTarget.FIELD, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class MockField(val type: KClass<*> , val value: String = "null", val attribute: String)
