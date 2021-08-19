package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ParticipantsItemBinding
import com.example.chatapp.databinding.RankingItemBinding
import com.example.chatapp.models.Profile
import com.example.chatapp.utils.MainApplication
import java.io.File

class RankingAdapter(private val profiles: ArrayList<Profile>) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private val context = MainApplication.getContextInstance()

    abstract inner class RankingBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(profile: Profile)
    }

    inner class RankingViewHolder(private val binding: RankingItemBinding) :
        RankingBaseViewHolder(binding.root) {
        override fun bind(profile: Profile) {
            val file = File(profile.photoProfile)
            with(binding) {
                if (file.exists()) {
                    imageProfile.setImageURI(file.toUri())
                } else {
                    imageProfile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
                }
                imageProfile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))

                name.text = profile.name
                imageRanking.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_gold_medal))
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return RankingViewHolder(RankingItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun onBindViewHolder(rankingHolder: RankingViewHolder, position: Int) {
        rankingHolder.bind(profiles[position])
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

}