package com.example.cookper

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_load.view.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    private var database = FirebaseFirestore.getInstance()
    private lateinit var dialogview: View
    private lateinit var diag: AlertDialog.Builder
    private var isMakeMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        diag = AlertDialog.Builder(this)
        dialogview = layoutInflater.inflate(R.layout.dialog_load, null)

        diag.setView(dialogview)
        dialogview.recipeCode.filters = (arrayOf<InputFilter>(InputFilter.AllCaps()))
        diag.setPositiveButton(R.string.diag_yes_button) { _, _ ->
            /* 확인일 때 main의 View의 값에 dialog View에 있는 값을 적용 */
            val code = dialogview.recipeCode.text.toString()
                database.collection("recipe").whereEqualTo("code", code).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        alert(title = "레시피가 존재하지 않습니다!", message = "레시피 공유 코드를 확인해주세요.") {
                            positiveButton("확인") {}
                        }.show()
                    } else {
                        isMakeMode = false
                        val r = documents.toObjects(Recipe::class.java)
                        startActivity<RecipeActivity>(
                            "mode" to isMakeMode,
                            "recipes" to r.last()
                        )
                    }
                }
                .addOnFailureListener { e ->
                    alert("인터넷에 연결되어 있는지 확인하세요.", "오류!") {
                        yesButton { }
                    }.show()
                }
        }
            .setNegativeButton(R.string.diag_no_button) { _, _ -> }


        ld_btn.setOnClickListener {
            if (dialogview.parent != null)
                (dialogview.parent as ViewGroup).removeView(dialogview)
            diag.show()
        }

        mk_btn.setOnClickListener {
            isMakeMode = true
            startActivity<RecipeActivity>("mode" to isMakeMode)
        }
    }
}