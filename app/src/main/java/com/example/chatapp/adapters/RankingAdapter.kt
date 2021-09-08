package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
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

            with(binding) {

                val photo = profile.photoProfile

                if (photo != null) {
                    val file = File(photo)
                    imageProfile.setImageURI(file.toUri())
                } else {
                    imageProfile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
                }

                name.text = profile.name
                points.text = context.getString(R.string.points, profile.scoreTicTacToe)

                when (layoutPosition) {
                    0 -> imageRanking.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gold_medal))
                    1 -> imageRanking.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_silver_medal))
                    2 -> imageRanking.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bronze_medal))
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return RankingViewHolder(RankingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(rankingHolder: RankingViewHolder, position: Int) {
        rankingHolder.bind(profiles[position])
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    fun addProfile(profile: Profile){
        profiles.forEach {
            if(it.id == profile.id){
                return
            }
        }
        this.profiles.add(profile)
        notifyItemInserted(this.profiles.indices.last)
    }

    fun removeProfile(id: Int){
        profiles.forEach {
            if(it.id == id){
                profiles.remove(it)
                return@forEach
            }
        }
    }


}