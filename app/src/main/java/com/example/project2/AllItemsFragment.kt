package com.example.project2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.databinding.AddRecommendationLayoutBinding
import com.example.project2.databinding.AllRecommendationsLayoutBinding

class AllItemsFragment : Fragment(){
    private var _binding : AllRecommendationsLayoutBinding? = null
    private val binding get()= _binding!!
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

        binding.recycel.adapter=ItemAdapter(ItemManager.items,object : ItemAdapter.ItemListener{
            override fun onItemClicked(index: Int) {
                Toast.makeText(requireContext(),"${ItemManager.items[index]}",Toast.LENGTH_SHORT).show()            }

            override fun onItemLongClicked(index: Int) {
                ItemManager.remove(index)
                binding.recycel.adapter!!.notifyItemRemoved(index)
            }
        })

        binding.recycel.layoutManager=LinearLayoutManager(requireContext())
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
                ItemManager.remove(viewHolder.adapterPosition)
                binding.recycel.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recycel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}