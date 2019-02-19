package wottrich.com.mock_annotations

/**
 * @author lucas.wottrich
 * @since 01/02/2019
 */

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class MockModel(val serializable: Boolean = false, val serializedName: Boolean = false)
