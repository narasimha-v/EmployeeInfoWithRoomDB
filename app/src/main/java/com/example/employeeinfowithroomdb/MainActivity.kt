package com.example.employeeinfowithroomdb

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.employeeinfowithroomdb.databinding.ActivityMainBinding
import com.example.employeeinfowithroomdb.databinding.DialogUpdateBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (application as EmployeeApp).db.employeeDao()

        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDao)
        }

        lifecycleScope.launch {
            employeeDao.fetchAllEmployees().collect {
                val list = ArrayList(it)
                setUpListOfDataIntoRecyclerView(list, employeeDao)
            }
        }
    }

    private fun addRecord(employeeDao: EmployeeDao) {
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()

        if (email.isEmpty() || name.isEmpty()) {
            Toast.makeText(
                this@MainActivity,
                "Please enter name and email address",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            lifecycleScope.launch {
                employeeDao.insert(
                    EmployeeEntity(
                        name = name,
                        email = email
                    )
                )
                Toast.makeText(
                    applicationContext,
                    "Record saved",
                    Toast.LENGTH_SHORT
                ).show()
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
            }
        }
    }

    private fun setUpListOfDataIntoRecyclerView(
        employeesList: ArrayList<EmployeeEntity>,
        employeeDao: EmployeeDao
    ) {
        if (employeesList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(
                employeesList,
                { updateId -> updateRecordDialog(updateId, employeeDao) },
                { deleteId -> deleteRecordAlertDialog(deleteId, employeeDao) }
            )
            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id: Int, employeeDao: EmployeeDao) {
        val updateDialog = Dialog(
            this, R.style.themeDialog
        )
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect {
                binding.etUpdateName.setText(it.name)
                binding.etUpdateEmailId.setText(it.email)
            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty()) {
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(
                        applicationContext,
                        "Record updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateDialog.dismiss()
                }
            } else {
                Toast.makeText(
                    this,
                    "Please enter name and email.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id: Int, employeeDao: EmployeeDao) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete employee info")
        builder.setIcon(android.R.drawable.ic_delete)

        builder.setPositiveButton("Delete") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted",
                    Toast.LENGTH_SHORT
                ).show()
                dialogInterface.dismiss()
            }
        }

        builder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}