package com.fauzan.kafeserver

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.databinding.ActivityHomeBinding
import com.fauzan.kafeserver.model.EventBus.CategoryClick
import com.fauzan.kafeserver.model.EventBus.ChangeMenuClick
import com.fauzan.kafeserver.model.EventBus.ToastEvent
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private var menuClick = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarHome.toolbar)

        drawer = binding.drawerLayout
        navigationView = binding.navView

        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_category, R.id.nav_food_list, R.id.nav_order, R.id.nav_refund
        )
            .setOpenableLayout(drawer)
            .build()

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(navigationView, navController)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.bringToFront()

        val headerView = navigationView.getHeaderView(0)
        val txtUser = headerView.findViewById<TextView>(R.id.txt_user)
        Common.currentServerUser?.name?.let { name ->
            Common.setSpanString("Hey, ", name, txtUser)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val nav = Navigation.findNavController(this, R.id.nav_host_fragment)
        return NavigationUI.navigateUp(nav, mAppBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategoryClick(event: CategoryClick) {
        if (event.isSuccess) {
            if (menuClick != R.id.nav_food_list) {
                navController.navigate(R.id.nav_food_list)
                menuClick = R.id.nav_food_list
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onToastEvent(event: ToastEvent) {
        if (event.isUpdate) {
            Toast.makeText(this, "Update Success!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Delete Success!", Toast.LENGTH_SHORT).show()
        }
        EventBus.getDefault().postSticky(ChangeMenuClick(isFromFoodList = event.isFromFoodList))
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onChangeMenuClick(event: ChangeMenuClick) {
        if (event.isFromFoodList) {
            navController.popBackStack(R.id.nav_category, true)
            navController.navigate(R.id.nav_category)
        } else {
            navController.popBackStack(R.id.nav_food_list, true)
            navController.navigate(R.id.nav_food_list)
        }
        menuClick = -1
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true
        drawer.closeDrawers()
        val itemId = menuItem.itemId
        if (itemId == R.id.nav_category) {
            if (itemId != menuClick) {
                navController.navigate(R.id.nav_category)
            }
        } else if (itemId == R.id.nav_order) {
            if (itemId != menuClick) {
                navController.navigate(R.id.nav_order)
            }
        } else if (itemId == R.id.nav_refund) {
            if (itemId != menuClick) {
                navController.navigate(R.id.nav_refund)
            }
        } else if (itemId == R.id.nav_sign_out) {
            signOut()
        } else {
            menuClick = -1
        }
        menuClick = menuItem.itemId
        return true
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Signout")
            .setMessage("Do you really want to sign out?")
            .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("OK") { _, _ ->
                Common.selectedFood = null
                Common.categorySelected = null
                Common.currentServerUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        builder.create().show()
    }
}
