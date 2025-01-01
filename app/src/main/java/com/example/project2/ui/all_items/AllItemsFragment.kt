package com.example.project2.ui.all_items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.databinding.AllRecommendationsLayoutBinding
import com.example.project2.ui.ItemsViewModel

class AllItemsFragment : Fragment(){
    private var _binding : AllRecommendationsLayoutBinding? = null
    private val binding get()= _binding!!

    private val viewModel : ItemsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AllRecommendationsLayoutBinding.inflate(inflater,container,false)
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_allItemsFragment_to_addItemFragment)

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("title")?.let {
            Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.items?.observe(viewLifecycleOwner){
            binding.recycler.adapter= ItemAdapter(it,object : ItemAdapter.ItemListener {
                override fun onItemClicked(index: Int) {
                    Toast.makeText(requireContext(),"${it[index]}",Toast.LENGTH_SHORT).show()            }

                override fun onItemLongClicked(index: Int) {
                    val item = (binding.recycler.adapter as ItemAdapter).itemAt(index)

                    viewModel.setItem(item)

                    findNavController().navigate(R.id.action_allItemsFragment_to_itemDetailsFragment)
                }
            })
        }



        binding.recycler.layoutManager=LinearLayoutManager(requireContext())
        ItemTouchHelper(object : ItemTouchHelper.Callback(){
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            )= makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT )

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = (binding.recycler.adapter as ItemAdapter).itemAt(viewHolder.adapterPosition)
                viewModel.deleteItem(item)
                //ItemManager.remove(viewHolder.adapterPosition)
                //binding.recycel.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recycler)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}