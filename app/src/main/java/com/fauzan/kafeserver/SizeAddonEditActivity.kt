package com.fauzan.kafeserver

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fauzan.kafeserver.adapter.MyAddonAdapter
import com.fauzan.kafeserver.adapter.MySizeAdapter
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.databinding.ActivitySizeAddonEditBinding
import com.fauzan.kafeserver.model.AddonModel
import com.fauzan.kafeserver.model.EventBus.AddonSizeEditEvent
import com.fauzan.kafeserver.model.EventBus.SelectAddonModel
import com.fauzan.kafeserver.model.EventBus.SelectSizeModel
import com.fauzan.kafeserver.model.EventBus.UpdateAddonModel
import com.fauzan.kafeserver.model.EventBus.UpdateSizeModel
import com.fauzan.kafeserver.model.SizeModel
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Activity for configuring food sizes and addons.
 * Migrated to Android Jetpack ViewBinding, eliminating ButterKnife.
 */
class SizeAddonEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySizeAddonEditBinding
    private var adapter: MySizeAdapter? = null
    private var addonAdapter: MyAddonAdapter? = null
    private var foodEditPosition = -1
    private var needSave = false
    private var isAddon = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySizeAddonEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.recyclerAddonSize.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerAddonSize.layoutManager = layoutManager
        binding.recyclerAddonSize.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        binding.btnCreate.setOnClickListener { onCreateNew() }
        binding.btnEdit.setOnClickListener { onEdit() }
    }

    private fun onCreateNew() {
        val name = binding.edtName.text.toString().trim()
        val price = binding.edtPrice.text.toString().trim().toLongOrNull() ?: 0L

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isAddon) {
            adapter?.addNewSize(SizeModel(name = name, price = price))
        } else {
            addonAdapter?.addNewSize(AddonModel(name = name, price = price))
        }

        binding.edtName.setText("")
        binding.edtPrice.setText("0")
    }

    private fun onEdit() {
        val name = binding.edtName.text.toString().trim()
        val price = binding.edtPrice.text.toString().trim().toLongOrNull() ?: 0L

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isAddon) {
            adapter?.editSize(SizeModel(name = name, price = price))
        } else {
            addonAdapter?.editSize(AddonModel(name = name, price = price))
        }

        binding.edtName.setText("")
        binding.edtPrice.setText("0")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.addon_size_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            saveData()
            return true
        } else if (item.itemId == android.R.id.home) {
            if (needSave) {
                AlertDialog.Builder(this)
                    .setTitle("Cancel?")
                    .setMessage("Do you really want to close without saving?")
                    .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("OK") { _, _ ->
                        needSave = false
                        closeActivity()
                    }
                    .create()
                    .show()
            } else {
                closeActivity()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveData() {
        val catSelected = Common.categorySelected
        val selectedFood = Common.selectedFood
        if (foodEditPosition != -1 && catSelected != null && selectedFood != null) {
            catSelected.foods?.set(foodEditPosition, selectedFood)

            val updateData = HashMap<String, Any?>()
            updateData["foods"] = catSelected.foods

            val catId = catSelected.menu_id ?: return
            FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(catId)
                .updateChildren(updateData as Map<String, Any>)
                .addOnFailureListener { e ->
                    Toast.makeText(this@SizeAddonEditActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                        needSave = false
                        binding.edtPrice.setText("0")
                        binding.edtName.setText("")
                    }
                }
        }
    }

    private fun closeActivity() {
        binding.edtName.setText("")
        binding.edtPrice.setText("0")
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel::class.java)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonSizeReceive(event: AddonSizeEditEvent?) {
        val food = Common.selectedFood
        if (event == null || food == null) return

        isAddon = event.isAddon
        foodEditPosition = event.pos

        if (!event.isAddon) {
            food.size?.let { sizeList ->
                adapter = MySizeAdapter(this, sizeList)
                binding.recyclerAddonSize.adapter = adapter
            }
        } else {
            food.addon?.let { addonList ->
                addonAdapter = MyAddonAdapter(this, addonList)
                binding.recyclerAddonSize.adapter = addonAdapter
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSizeModelUpdate(event: UpdateSizeModel?) {
        val food = Common.selectedFood
        if (event?.sizeModelList != null && food != null) {
            needSave = true
            food.size = event.sizeModelList
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonModelUpdate(event: UpdateAddonModel?) {
        val food = Common.selectedFood
        if (event?.addonModel != null && food != null) {
            needSave = true
            food.addon = event.addonModel
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectSizeModel(event: SelectSizeModel?) {
        val size = event?.sizeModel
        if (size != null) {
            binding.edtName.setText(size.name)
            binding.edtPrice.setText(size.price.toString())
            binding.btnEdit.isEnabled = true
        } else {
            binding.btnEdit.isEnabled = false
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectAddonModel(event: SelectAddonModel?) {
        val addon = event?.addonModel
        if (addon != null) {
            binding.edtName.setText(addon.name)
            binding.edtPrice.setText(addon.price.toString())
            binding.btnEdit.isEnabled = true
        } else {
            binding.btnEdit.isEnabled = false
        }
    }
}
