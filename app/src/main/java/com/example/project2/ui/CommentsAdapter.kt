package com.example.project2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R

class CommentsAdapter(private var comments: MutableList<String>) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val commentText: TextView = itemView.findViewById(R.id.comment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val commentParts = comments[position].split(": ", limit = 2)
        val userName = commentParts.getOrNull(0) ?: "אנונימי"
        val commentText = commentParts.getOrNull(1) ?: comments[position]

        holder.userName.text = userName
        holder.commentText.text = commentText
    }


    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<String>) {
        comments = newComments.toMutableList()
        notifyDataSetChanged()
    }


}
