import retrofit2.http.GET
import com.accompany.purchaseManagement.Cattle

interface CattleSheetsApi {
    @GET(".")  // 웹앱 root로 요청
    suspend fun getCattleList(): List<Cattle>
}
