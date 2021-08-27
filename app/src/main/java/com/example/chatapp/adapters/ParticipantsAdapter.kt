package com.example.chatapp.adapters

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ParticipantsItemBinding
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.viewModel.ConnectionFactory
import java.io.File

class ParticipantsAdapter(private val profiles: ArrayList<Profile>, private val connectionFactory: ConnectionFactory) :
    RecyclerView.Adapter<ParticipantsAdapter.BaseViewHolder>() {
    private var iamAdmin = verifyIfIsAdmin()
    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(profile: Profile)
    }

    private inner class ProfileViewHolder(private val binding: ParticipantsItemBinding) : BaseViewHolder(binding.root) {
        override fun bind(profile: Profile) {
            val file = File(profile.photoProfile?: "")
            with(binding) {
//                if (file.exists() && profile.photoProfile != "" && file.isFile) {
                try{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val bitMap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(MainApplication.getContextInstance().contentResolver,file.toUri()))
                        imageProfile.setImageBitmap(bitMap)
                    }else{
                        BitmapFactory.decodeFileDescriptor(file.toUri().let {
                            MainApplication.getContextInstance().contentResolver.openFileDescriptor(it, "r")?.fileDescriptor
                        })
                    }
                } catch (e: Exception) {
                    Log.e("ParticipantsAdapter", "Error set drawable profile image $e")
                    val context = MainApplication.getContextInstance()
                    imageProfile.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_profile
                        )
                    )
                }
                name.text = profile.name
                kickButton.apply {
                    if(iamAdmin){
                        kickButton.visibility = View.VISIBLE
                    }
                    setOnClickListener {
                        val message = Message(Message.MessageType.REVOKED.code, id = 3, text = profile.id.toString(), base64Data = "", username = "")
                        connectionFactory.sendMessageToSocket(message){}
                    }
                }
                if(profile.isAdmin == true){
                    adminTitle.visibility = View.VISIBLE
                }else{
                    adminTitle.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ProfileViewHolder(
            ParticipantsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int {
        return profiles.size
    }
    private fun verifyIfIsAdmin(): Boolean{
        profiles.forEach {
            if(it.isAdmin == true){
                if(it.id == ProfileSharedProfile.getIdProfile()){
                    return true
                }
            }
        }
        return false
    }
}