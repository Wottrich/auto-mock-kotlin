package wottrich.com.mock_annotations


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class MockList(val list: Array<String>)