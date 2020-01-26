package com.example.cookper

import android.content.Context
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.format.DateUtils
import android.view.Gravity
import kotlinx.android.synthetic.main.dialog_alarm.view.*

class RecipeAdapter(val context: Context, val recipeList: ArrayList<Cook>) : BaseAdapter() {

    lateinit var mediaPlayer: MediaPlayer

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.cooking, null)

        val recipeNum = view.findViewById<TextView>(R.id.num)
        val recipeCook = view.findViewById<TextView>(R.id.cook_tv)
        val recipeFood = view.findViewById<TextView>(R.id.food_tv)
        val recipeTimer = view.findViewById<LinearLayout>(R.id.timer_layout)
        val recipeTime = view.findViewById<TextView>(R.id.timer)
        val timerbtn = view.findViewById<Button>(R.id.timer_btn)

        val recipe = recipeList[position]
        recipeNum.text = recipe.num.toString()
        recipeCook.text = recipe.cooking
        recipeFood.text = recipe.food

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm)

        if (recipe.timer != "0") {
            var wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
            }

            val popupView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.dialog_alarm, null)
            val mPopupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            mPopupWindow.setFocusable(true)
            mPopupWindow.setOnDismissListener {
                mediaPlayer.stop()
            }

            popupView.alarm_btn.setOnClickListener {
                mPopupWindow.dismiss()
            }

            var counting = false
            var time = (recipe.timer.toDouble() * 60).toLong()

            recipeTimer.visibility = View.VISIBLE
            recipeTime.text = DateUtils.formatElapsedTime(time)
            val timer = object : CountDownTimer((time * 100), 100) {
                override fun onFinish() {
                    time = (recipe.timer.toDouble() * 60).toLong()
                    timerbtn.text = "조리시작"
                    counting = false

                    if(!mediaPlayer.isPlaying)
                        mediaPlayer.start()

                    popupView.alarm_txt.text = recipe.cooking
                    mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                    wakeLock.apply {
                        release()
                    }
                }

                override fun onTick(millisUntilFinished: Long) {
                    time--
                    recipeTime.text = DateUtils.formatElapsedTime(time)
                }
            }
            timerbtn.setOnClickListener {
                if (counting) {
                    timer.cancel()
                    timerbtn.text = "조리시작"
                    wakeLock.apply {
                        release()
                    }
                } else {
                    timer.start()
                    timerbtn.text = "일시정지"
                    wakeLock.apply {
                        acquire()
                    }
                }
                counting = !counting
            }

            timerbtn.setOnLongClickListener {
                time = (recipe.timer.toDouble() * 60).toLong()
                recipeTime.text = DateUtils.formatElapsedTime(time)
                timer.cancel()
                counting = false
                timerbtn.text = "조리시작"

                return@setOnLongClickListener true
            }
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return recipeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return recipeList.size
    }
}