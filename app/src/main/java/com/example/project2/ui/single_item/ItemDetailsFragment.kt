package com.example.project2.ui.single_item

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.example.project2.databinding.FragmentItemDetailsBinding
import com.example.project2.ui.FullImageActivity
import com.example.project2.ui.ItemsViewModel


class ItemDetailsFragment : Fragment() {
    private var _binding: FragmentItemDetailsBinding? = null

    val viewModel: ItemsViewModel by activityViewModels()
    private lateinit var commentsAdapter: CommentsAdapter

    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemDetailsBinding.inflate(inflater, container, false)


        return binding.root
    }



//    private fun setupCategoryButtons(categories: List<String>) {
//        // איפוס התצוגה
//        binding.categoryContainer.removeAllViews()
//
//        if (categories.isEmpty()) {
//            val noCategoryText = TextView(requireContext()).apply {
//                text = getString(R.string.no_category)
//                textSize = 16f
//                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
//                setPadding(8, 8, 8, 8)
//            }
//            binding.categoryContainer.addView(noCategoryText)
//        } else {
//            // יצירת כפתורים עבור קטגוריות שנבחרו
//            categories.forEach { category ->
//                val button = Button(requireContext()).apply {
//                    text = category
//                    textSize = 16f
//                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
//                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//                    setPadding(8, 8, 8, 8)
//                }
//                binding.categoryContainer.addView(button)
//            }
//        }
//    }


    private fun setupCategoryText(categories: List<String>) {
        val formattedCategories = if (categories.isEmpty()) {
            getString(R.string.no_category)
        } else {
            categories.joinToString(" | ") // מחבר את הקטגוריות עם קו מפריד "|"
        }

        binding.itemCategory.text = formattedCategories
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // צופה בנתונים שנבחרו ב-ViewModel
        viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
            val title = item.title.ifBlank { getString(R.string.no_title) }
            val link = item.link.ifBlank { "" }

            binding.itemTitle.text = title

            if (link.isNotEmpty()) {
                binding.itemTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700))
                binding.itemTitle.paint.isUnderlineText = true // מוסיף קו תחתון כדי להראות שזה לינק
                binding.itemTitle.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }
            } else {
                binding.itemTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.itemTitle.paint.isUnderlineText = false
                binding.itemTitle.setOnClickListener(null) // מבטל את היכולת ללחוץ
            }


            binding.itemComment.text = if (item.comment.isBlank()) {
                getString(R.string.no_comment)
            } else {
                "\"${item.comment}\""
            }
            binding.itemPrice.text =
                if (item.price == 0.0) getString(R.string.no_price) else " ${item.price}"
            binding.addressTextView.text = item.address?.ifBlank { "No address" }

            // עדכון תמונה
            if (item.photo.isNullOrEmpty()) {
                binding.itemImage.setImageResource(R.drawable.baseline_hide_image_24)
            } else {
                binding.itemImage.setImageURI(Uri.parse(item.photo))
            }

            // עדכון דירוג כוכבים
            val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < item.rating) R.drawable.star_full else R.drawable.star_empty
                )
            }

            // עדכון קטגוריות
            setupCategoryText(item.category.split(", ").filter { it.isNotBlank() })
        }


            viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
                val address = item.address?.ifBlank { null } // אם הכתובת ריקה, נתייחס אליה כ-null

                if (address.isNullOrEmpty()) {
                    binding.addressTextView.visibility = View.GONE
                    binding.showAddressButton.visibility = View.GONE
                    binding.locationIcon.visibility = View.GONE // הוספת הסתרה לאייקון
                } else {
                    binding.addressTextView.text = address
                    binding.addressTextView.visibility = View.VISIBLE
                    binding.showAddressButton.visibility = View.VISIBLE
                    binding.locationIcon.visibility = View.VISIBLE // הצגת האייקון אם יש כתובת
                }
            }

            binding.showAddressButton.setOnClickListener {
                viewModel.chosenItem.value?.let { item ->
                    if (!item.address.isNullOrEmpty()) {
                        val bundle = Bundle().apply {
                            putString("address", item.address)
                        }
                        findNavController().navigate(
                            R.id.action_itemDetailsFragment_to_mapFragment,
                            bundle
                        )
                    } else {
                        Toast.makeText(requireContext(), "No address to check", Toast.LENGTH_SHORT).show()
                    }
                }
            }


// התחברות ל-ImageView באמצעות binding
        val itemImageView = binding.itemImage

        // קבלת ה-imageUrl מהפריט הנבחר ב-ViewModel
        val item = viewModel.chosenItem.value
        val imageUrl = item?.photo

        // הגדרת מאזין לחיצה על התמונה
        itemImageView.setOnClickListener {
            imageUrl?.let { url ->
                // קריאה ל-FullImageActivity והעברת הנתיב של התמונה
                val intent = Intent(requireContext(), FullImageActivity::class.java)
                intent.putExtra("imageUri", url)
                startActivity(intent)
            } ?: run {
                Toast.makeText(requireContext(), "אין תמונה להצגה", Toast.LENGTH_SHORT).show()
            }
        }


            val commentsRecyclerView = binding.root.findViewById<RecyclerView>(R.id.comments_recycler_view)
            val commentInput = binding.root.findViewById<EditText>(R.id.comment_input)
            val addCommentButton = binding.root.findViewById<Button>(R.id.add_comment_button)

            // אתחול ה-RecyclerView
            commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            commentsAdapter = CommentsAdapter(mutableListOf())
            commentsRecyclerView.adapter = commentsAdapter

            // ✅ טוען את התגובות מה-ViewModel בעת הצגת הפריט
            viewModel.chosenItem.observe(viewLifecycleOwner) { item ->
                val commentsList = item.comments ?: emptyList()
                commentsAdapter.updateComments(commentsList.toMutableList()) // עדכון ה-RecyclerView עם כל התגובות
            }

            // מאזין ללחיצה על כפתור הוספת תגובה
            addCommentButton.setOnClickListener {
                val newComment = commentInput.text.toString().trim()
                if (newComment.isNotEmpty()) {
                    val item = viewModel.chosenItem.value ?: return@setOnClickListener
                    val commentsList = item.comments.toMutableList() // שמירה על התגובות הקיימות

                    // ✅ הוספת תגובה לרשימה הקיימת
                    commentsList.add(newComment)

                    // ✅ עדכון התגובות ב-Database וב-RecyclerView
                    viewModel.updateItemComments(item, commentsList)

                    // ✅ עדכון ה-RecyclerView כדי לשמור את כל התגובות
                    commentsAdapter.updateComments(commentsList)

                    // ✅ ניקוי שדה הקלט
                    commentInput.text.clear()
                }
            }
        commentInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                commentsRecyclerView.postDelayed({
                    commentsRecyclerView.scrollToPosition(commentsAdapter.itemCount - 1) // ✅ מבטיח שהגלילה תישמר
                }, 200)
            }
        }






    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
