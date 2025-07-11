import retrofit2.http.GET

interface CattleSheetsApi {
    @GET(".")  // 웹앱 root로 요청
    suspend fun getCattleList(): List<Cattle>
}
