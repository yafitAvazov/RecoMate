//import com.example.project2.utils.Resource
//
//
//inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
//    return try {
//        action()
//    } catch (e: Exception) {
//        Resource.error(e.message ?: "An unknown Error Occurred")
//    }
//}