package com.fauzan.kafeserver

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fauzan.kafeserver.common.Common
import com.fauzan.kafeserver.model.ServerUserModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private var dialog: AlertDialog? = null
    private lateinit var serverRef: DatabaseReference
    private lateinit var providers: List<AuthUI.IdpConfig>

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        providers = listOf(AuthUI.IdpConfig.PhoneBuilder().build())

        serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(true).build()

        listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                checkServerUserFromFirebase(user)
            } else {
                phoneLogin()
            }
        }
    }

    private fun checkServerUserFromFirebase(user: FirebaseUser) {
        dialog?.show()
        serverRef.child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userModel = dataSnapshot.getValue(ServerUserModel::class.java)
                        if (userModel?.isActive == true) {
                            goToHomeActivity(userModel)
                        } else {
                            dialog?.dismiss()
                            Toast.makeText(
                                this@MainActivity,
                                "You must be allowed from Admin to access this app",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        dialog?.dismiss()
                        showRegisterDialog(user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    dialog?.dismiss()
                    Toast.makeText(this@MainActivity, databaseError.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showRegisterDialog(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Register")
        builder.setMessage("Please fill information \n Admin will accept your account later")

        val itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null)
        val edtName = itemView.findViewById<EditText>(R.id.edt_name)
        val edtPhone = itemView.findViewById<EditText>(R.id.edt_phone)

        edtPhone.setText(user.phoneNumber)
        builder.setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("REGISTER") { _, _ ->
                val name = edtName.text.toString().trim()
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this@MainActivity, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val serverUserModel = ServerUserModel(
                    uid = user.uid,
                    name = name,
                    phone = edtPhone.text.toString(),
                    isActive = false
                )

                dialog?.show()

                serverRef.child(serverUserModel.uid ?: "")
                    .setValue(serverUserModel)
                    .addOnFailureListener { e ->
                        dialog?.dismiss()
                        Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        dialog?.dismiss()
                        Toast.makeText(
                            this@MainActivity,
                            "Registration success! Admin will verify and activate your account soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        builder.setView(itemView)
        builder.create().show()
    }

    private fun goToHomeActivity(serverUserModel: ServerUserModel) {
        dialog?.dismiss()
        Common.currentServerUser = serverUserModel
        Common.syncToSession()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun phoneLogin() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            APP_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val APP_REQUEST_CODE = 7171
    }
}
