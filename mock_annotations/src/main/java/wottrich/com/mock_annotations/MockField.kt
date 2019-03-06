package wottrich.com.mock_annotations

import kotlin.reflect.KClass

/**
 * @author lucas.wottrich
 * @since 30/01/2019
 */

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class MockField(
    val type: KClass<*>,
    val value: String = "null",
    val attribute: String
)
