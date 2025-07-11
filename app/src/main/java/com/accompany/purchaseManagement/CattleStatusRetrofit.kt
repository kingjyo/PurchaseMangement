import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CattleStatusRetrofit {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbzUPcgAfT0WUb47HDvdfYY-wQrtsxkDseovQRxgFOaoWm4KzsxR8bXDU2q5M7JtQAOHJA/exec/"

    val api: CattleSheetsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CattleSheetsApi::class.java)
    }
}
