import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.accompany.purchaseManagement.R
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.activityViewModels
import com.accompany.purchaseManagement.PurchaseViewModel

class QuantityFragment : Fragment() {

    private lateinit var etQuantity: EditText
    private lateinit var tvUnit: TextView

    private val viewModel: PurchaseViewModel by activityViewModels()  // 공유 ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quantity, container, false)

        etQuantity = view.findViewById(R.id.etQuantity)
        tvUnit = view.findViewById(R.id.tvUnit)

        // 초기 값 "1"로 설정 (ViewModel에서 가져와 복원)
        etQuantity.setText(viewModel.quantity.value ?: "1")

        // 포커스 설정
        etQuantity.requestFocus()

        // TextWatcher로 실시간 ViewModel 업데이트
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                viewModel.quantity.value = if (input.isEmpty()) "1" else input  // 빈 경우 "1"로
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    // 수량 가져오기 (ViewModel 우선)
    fun getQuantity(): String {
        return viewModel.quantity.value ?: etQuantity.text.toString().trim()
    }
}
