import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.accompany.purchaseManagement.R

class QuantityFragment : Fragment() {

    private lateinit var etQuantity: EditText
    private lateinit var tvUnit: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quantity, container, false)

        etQuantity = view.findViewById(R.id.etQuantity)
        tvUnit = view.findViewById(R.id.tvUnit)

        etQuantity.setText("1")
        etQuantity.setSelection(etQuantity.text.length)

        etQuantity.requestFocus()

        return view
    }

    fun getQuantity(): String = etQuantity.text.toString().trim()
}