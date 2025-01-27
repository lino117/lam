package com.example.progettolam

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.example.progettolam.fragment.HistoryFragment
import com.example.progettolam.fragment.homeFragment
import com.example.progettolam.fragment.statisticFragment

class DynamicTestMainActivity : AppCompatActivity(), View.OnClickListener {
    private var llTime: LinearLayout? = null
    private var llRecords: LinearLayout? = null
    private var llStatistic: LinearLayout? = null
    private var ivTime: ImageView? = null
    private var ivRecords: ImageView? = null
    private var ivStatistic: ImageView? = null
    private var tvTime: TextView? = null
    private var tvRecords: TextView? = null
    private var tvStatistic: TextView? = null
    private val tvActiviting: TextView? = null

    var fragmentManager: FragmentManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_dynamic_test_main)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //        dpHelper = new activityRecordDbHelper(this);
        initView()
        initEvent()
    }

    private fun initView() {
        llTime = findViewById(R.id.ll_time)
        llRecords = findViewById(R.id.ll_records)
        llStatistic = findViewById(R.id.ll_statistic)

        ivTime = findViewById(R.id.iv_time)
        ivRecords = findViewById(R.id.iv_records)
        ivStatistic = findViewById(R.id.iv_statistic)

        tvTime = findViewById(R.id.tv_time)
        tvRecords = findViewById(R.id.tv_records)
        tvStatistic = findViewById(R.id.tv_statistic)
    }

    private fun initEvent() {
        fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        // 第三个 arg 可以接受一个bundle可以用来传输数据， 然后由dynamicFragment.class. onCreate 接受并使用
        // Budle bundle = new budle();
        // budle.putString("param1" 跟class里面的变量一样的名字或者把变量改成private然后用name.class.ARG_PARAM1来决定，“text, message”)
        fragmentTransaction.replace(R.id.fcv_fragment, homeFragment::class.java, null)
            .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
            .setReorderingAllowed(true) // 用来辅助返回
            .commit()
        setActiveBottomNavItem(R.id.ll_time)
        llTime!!.setOnClickListener(this)
        llRecords!!.setOnClickListener(this)
        llStatistic!!.setOnClickListener(this)
    }

    private fun resetBottomNavItem() {
        ivTime!!.isSelected = false
        tvTime!!.setTextColor(getColor(R.color.disable_color))
        ivRecords!!.isSelected = false
        tvRecords!!.setTextColor(getColor(R.color.disable_color))
        ivStatistic!!.isSelected = false
        tvStatistic!!.setTextColor(getColor(R.color.disable_color))
    }

    private fun setActiveBottomNavItem(id: Int) {
        if (id == R.id.ll_time) {
            ivTime!!.isSelected = true
            tvTime!!.setTextColor(getColor(R.color.active_color))
        } else if (id == R.id.ll_records) {
            ivRecords!!.isSelected = true
            tvRecords!!.setTextColor(getColor(R.color.active_color))
        } else if (id == R.id.ll_statistic) {
            ivStatistic!!.isSelected = true
            tvStatistic!!.setTextColor(getColor(R.color.active_color))
        }
    }

    override fun onClick(view: View) {
        val id = view.id
        resetBottomNavItem()

        if (id == R.id.ll_time) {
            fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            // 第三个 arg 可以接受一个bundle可以用来传输数据， 然后由dynamicFragment.class. onCreate 接受并使用
            // Budle bundle = new budle();
            // budle.putString("param1" 跟class里面的变量一样的名字或者把变量改成private然后用name.class.ARG_PARAM1来决定，“text, message”)
            fragmentTransaction.replace(R.id.fcv_fragment, homeFragment::class.java, null)
                .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                .setReorderingAllowed(true) // 用来辅助返回
                .commit()
            setActiveBottomNavItem(id)
        } else if (id == R.id.ll_records) {
            fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()

            fragmentTransaction.replace(R.id.fcv_fragment, HistoryFragment::class.java, null)
                .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                .setReorderingAllowed(true) // 用来辅助返回
                .commit()
            setActiveBottomNavItem(id)
        } else if (id == R.id.ll_statistic) {
            fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.fcv_fragment, statisticFragment::class.java, null)
                .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                .setReorderingAllowed(true) // 用来辅助返回
                .commit()
            setActiveBottomNavItem(id)
        }
    }

    override fun onStop() {
        super.onStop()
    }
}