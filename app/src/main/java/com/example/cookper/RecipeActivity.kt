package com.example.cookper

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_recipe.*
import kotlinx.android.synthetic.main.dialog_add_recipe.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.security.MessageDigest


class RecipeActivity : AppCompatActivity() {

    private var recipeList = arrayListOf<Cook>()
    private lateinit var database: FirebaseFirestore

    private var num = 0
    private val recipeAdapter = RecipeAdapter(this, recipeList)
    private var saved = false

    private lateinit var dialogview: View
    private lateinit var diag: AlertDialog.Builder

    private lateinit var dialogviewtoedit: View
    private lateinit var diagtoedit: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        database = FirebaseFirestore.getInstance()

        recipeListView.adapter = recipeAdapter

        val mode = intent.getBooleanExtra("mode", false)
        val recipes = intent.getParcelableExtra<Recipe>("recipes")
        uploadRecipe_btn.show()
        if (mode) {
            uploadRecipe_btn.show()
        } else {
            recipes.cooks?.forEach {
                recipeList.add(it)
            }
            num = recipes.count
            //saved = true
        }

        diag = AlertDialog.Builder(this)
        diagtoedit = AlertDialog.Builder(this)
        dialogview = layoutInflater.inflate(R.layout.dialog_add_recipe, null)
        dialogviewtoedit = layoutInflater.inflate(R.layout.dialog_add_recipe, null)

        diag.setView(dialogview)
        diagtoedit.setView(dialogviewtoedit)

        recipeListView.setOnItemLongClickListener { _, _, position, _ ->

            val item = recipeList[position]

            diagtoedit.setPositiveButton(R.string.diag_add_button) { _, _ ->
                /* 확인일 때 main의 View의 값에 dialog View에 있는 값을 적용 */
                item.cooking = dialogviewtoedit.cooking_et.text.toString()
                item.food = dialogviewtoedit.food_et.text.toString()
                item.timer = if (dialogviewtoedit.time_et.text.toString().isNotEmpty()) {
                    if (dialogviewtoedit.time_et.text.toString() != "0" && dialogviewtoedit.time_et.text.startsWith("0"))
                        dialogviewtoedit.time_et.setText(dialogviewtoedit.time_et.text.substring(1, dialogviewtoedit.time_et.text.length))
                    dialogviewtoedit.time_et.text.toString()
                } else {
                    "0"
                }

                dialogviewtoedit.cooking_et.setText("")
                dialogviewtoedit.food_et.setText("")
                dialogviewtoedit.time_et.setText("0")
                recipeAdapter.notifyDataSetChanged()
                saved = false
                uploadRecipe_btn.show()

            }
                .setNegativeButton(R.string.diag_no_button) { _, _ -> }
                .setNeutralButton("삭제") { _, _ ->
                    recipeList.remove(recipeList[position])
                    num--;
                    var setnum = 1;
                    for (recipe in recipeList) {
                        recipe.num = setnum++
                    }
                    recipeAdapter.notifyDataSetChanged()
                }

            if (dialogviewtoedit.parent != null)
                (dialogviewtoedit.parent as ViewGroup).removeView(dialogviewtoedit)

            dialogviewtoedit.cooking_et.setText(item.cooking)
            dialogviewtoedit.food_et.setText(item.food)
            dialogviewtoedit.time_et.setText(item.timer)
            diagtoedit.show()


            return@setOnItemLongClickListener true
        }

        addRecipe_btn.setOnClickListener {
            diag.setPositiveButton(R.string.diag_add_button) { _, _ ->
                recipeList.add(
                    Cook(
                        ++num,
                        dialogview.cooking_et.text.toString(),
                        dialogview.food_et.text.toString(),
                        if (dialogview.time_et.text.toString().isNotEmpty()) {
                            if (dialogview.time_et.text.toString() != "0" && dialogview.time_et.text.startsWith("0"))
                                dialogview.time_et.setText(dialogview.time_et.text.substring(1, dialogview.time_et.text.length))
                            dialogview.time_et.text.toString()
                        } else {
                            "0"
                        }
                    )
                )
                dialogview.cooking_et.setText("")
                dialogview.food_et.setText("")
                dialogview.time_et.setText("0")
                recipeAdapter.notifyDataSetChanged()
                saved = false
                uploadRecipe_btn.show()
            }
                .setNegativeButton(R.string.diag_no_button) { _, _ -> }

            if (dialogview.parent != null)
                (dialogview.parent as ViewGroup).removeView(dialogview)
            diag.show()
        }

        uploadRecipe_btn.setOnClickListener {
            if (num > 3 && !saved) {
                alert("레시피를 저장하시겠습니까?", "레시피 저장") {
                    yesButton {
                        database.collection("recipe").get()
                            .addOnSuccessListener { documents ->
                                val savingRecipe = Recipe("", num, "", recipeList)
                                val hash = hash(savingRecipe.toString())

                                for (d in documents) {
                                    if (d.data["hash"].toString() == hash) {
                                        alert(
                                            "레시피 저장이 완료되었습니다. 공유코드는 ${d.data["code"]} 입니다.",
                                            "레시피 저장"
                                        ) {
                                            saved = true
                                            uploadRecipe_btn.hide()
                                            yesButton { }
                                        }.show()
                                        return@addOnSuccessListener
                                    }
                                }
                                var code: String
                                do {
                                    var codeDup = false
                                    code = RandomCode().codeGenerator()
                                    for (d in documents) {
                                        if (d.data["code"].toString() == code)
                                            codeDup = true
                                    }
                                } while (codeDup)
                                savingRecipe.code = code
                                savingRecipe.hash = hash
                                database.collection("recipe")
                                    .add(savingRecipe)
                                    .addOnSuccessListener {
                                        alert("레시피 저장이 완료되었습니다. 공유코드는 $code 입니다.", "레시피 저장") {
                                            saved = true
                                            uploadRecipe_btn.hide()
                                            yesButton { }
                                        }.show()
                                    }
                                    .addOnFailureListener { e ->
                                        alert("레시피 저장에 실패했습니다. 인터넷에 연결되어 있는지 확인해주세요", "레시피 저장") {
                                            yesButton { }
                                        }.show()
                                        Log.d("FIREBASE", "Error adding document", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                alert("레시피 저장에 실패했습니다. 인터넷에 연결되어 있는지 확인해주세요", "레시피 저장") {
                                    yesButton { }
                                }.show()
                                Log.d("FIREBASE", "Error adding document", e)
                            }
                    }
                    noButton { }
                }.show()
            } else {
                alert("레시피는 4단계 이상을 추가하고 저장할 수 있습니다.", "레시피 저장") {
                    yesButton { }
                }.show()
            }
        }
    }

    fun hash(data : String): String {
        val bytes = data.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}
