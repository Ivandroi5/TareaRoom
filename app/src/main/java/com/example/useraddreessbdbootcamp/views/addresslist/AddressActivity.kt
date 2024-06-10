package com.example.useraddreessbdbootcamp.views.addresslist

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.useraddreessbdbootcamp.R
import com.example.useraddreessbdbootcamp.database.AppDataBase
import com.example.useraddreessbdbootcamp.entities.Address
import com.example.useraddreessbdbootcamp.entities.User
import com.example.useraddreessbdbootcamp.repository.MainRepository
import com.example.useraddreessbdbootcamp.viewmodels.address.AddressViewModel
import com.example.useraddreessbdbootcamp.viewmodels.address.AddressViewModelFactory
import com.example.useraddreessbdbootcamp.viewmodels.user.UserViewModel
import com.example.useraddreessbdbootcamp.viewmodels.user.UserViewModelFactory
import com.example.useraddreessbdbootcamp.views.listusers.UserListAdapter

class AddressActivity : AppCompatActivity() {

    private lateinit var viewModelAddress: AddressViewModel
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        val database = AppDataBase.getDatabase(application)
        val repository = MainRepository(database.userDao(), database.addressDao())
        val factory = AddressViewModelFactory(application, repository)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAddress)
        val adapter = AddressListAdapter { address -> onAddressClick(address) }

        viewModelAddress = ViewModelProvider(this, factory)[AddressViewModel::class.java]
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        userId = intent.getLongExtra("USER_ID", -1L)

        viewModelAddress.getAddressForUser(userId)

        viewModelAddress.usersLV.observe(this) { addess ->
            addess?.let { adapter.submitList(it) }
        }
        val addAddressButton: Button = findViewById(R.id.btn_add_address)

        addAddressButton.setOnClickListener {
            showAlertDialogInsertAddress()
        }
    }

        private fun onAddressClick(address: Address) {
            val optionsMenu = arrayOf("Actualizar Dirección", "Eliminar Dirección")
            AlertDialog.Builder(this)
                .setTitle("Selecciona una opción")
                .setItems(optionsMenu) { dialog, selected ->
                    when (selected) {
                        0 -> showUpdateAddress(address)
                        1 -> showDeleteAddress(address)

                    }
                }
                .show()

    }

        private fun showDeleteAddress(address: Address) {

            AlertDialog.Builder(this)
                .setTitle("Borrar Dirección")
                .setMessage("¿ Esta seguro que desea borrar esta dirección?")
                .setPositiveButton("Sí") { dialog, _ ->
                    viewModelAddress.deleteAddress(address, userId)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                .show()
        }

        private fun showAlertDialogInsertAddress() {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Agregar un Dirección")

            val layoutItem = LinearLayout(this)
            layoutItem.orientation = LinearLayout.VERTICAL

            val inputCity = EditText(this)
            inputCity.hint = "City"
            layoutItem.addView(inputCity)

            val inputStreet = EditText(this)
            inputStreet.hint = "Street"
            layoutItem.addView(inputStreet)

            val inputNumber = EditText(this)
            inputNumber.hint = "Number"
            inputNumber.inputType = InputType.TYPE_CLASS_NUMBER
            layoutItem.addView(inputNumber)

            builder.setView(layoutItem)
            builder.setPositiveButton("OK") { dialog, _ ->
                val city = inputCity.text.toString()
                val street = inputStreet.text.toString()
                val number = inputNumber.text.toString()
                if (street.isNotEmpty() && city.isNotEmpty() && number.isNotEmpty()) {
                    viewModelAddress.insertAddress(
                        Address(
                            userOwnerId = userId,
                            city = city,
                            street = street,
                            number = number
                        ), userId
                    )
                    Toast.makeText(this, "Agregando Dirección", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            builder.show()
        }
    private fun showUpdateAddress(address: Address) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar una Direccion")

        val layoutItem = LinearLayout(this)
        layoutItem.orientation = LinearLayout.VERTICAL

        val inputCity = EditText(this)
        inputCity.inputType = InputType.TYPE_CLASS_TEXT
        inputCity.setText(address.city)
        layoutItem.addView(inputCity)

        val inputStreet = EditText(this)
        inputStreet.inputType = InputType.TYPE_CLASS_TEXT
        inputStreet.setText(address.street)
        layoutItem.addView(inputStreet)

        val inputNumber = EditText(this)
        inputNumber.inputType = InputType.TYPE_CLASS_NUMBER
        inputNumber.setText(address.number)
        layoutItem.addView(inputNumber)

        builder.setView(layoutItem)

        builder.setPositiveButton("OK") { dialog, _ ->

            val street = inputStreet.text.toString()
            val city = inputCity.text.toString()
            val number = inputNumber.text.toString()

            address.city = city
            address.street = street
            address.number = number


            if (street.isNotEmpty() && city.isNotEmpty() && number.isNotEmpty()) {
                viewModelAddress.updateAddress(address, userId)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}


