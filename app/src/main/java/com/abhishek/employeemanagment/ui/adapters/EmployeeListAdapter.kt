package com.abhishek.employeemanagment.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.databinding.ItemEmployeeListBinding

class EmployeeListAdapter(
    private val listOfEmployees: List<EmployeeEntity>,
    private val nextIconCLickListener: (EmployeeEntity) -> Unit,
    private val selectListener: (IntArray) -> Unit
) :
    RecyclerView.Adapter<EmployeeListAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemEmployeeListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setDataForEmployee(
            employeeItem: EmployeeEntity,
            nextIconCLickListener: (EmployeeEntity) -> Unit,
            selectListener: (IntArray) -> Unit,
            position: Int
        ) {
            binding.apply {
                imageViewPerson.load(employeeItem.profilePicUrl)
                titleEmployee.text = employeeItem.name
                designationEmployee.text = employeeItem.designation
                binding.root.isLongClickable = true

                root.setOnClickListener {
                    nextIconCLickListener(employeeItem)
                }
                checkBoxSelect.setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                        selectListener(intArrayOf(position, 1))
                    } else {
                        selectListener(intArrayOf(position, 0))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEmployeeListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val employeeItem = listOfEmployees[position]
        holder.setDataForEmployee(employeeItem, nextIconCLickListener, selectListener, position)
    }

    override fun getItemCount() = listOfEmployees.size
}
