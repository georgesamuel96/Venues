package com.example.venues.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.venues.R
import com.example.venues.data.model.Venues
import com.example.venues.databinding.ItemVenuesBinding
import com.example.venues.utils.Util.getAddress
import com.example.venues.utils.Util.getIconURL

class VenuesAdapter(
    private val venuesList: MutableList<Venues>
) : RecyclerView.Adapter<VenuesAdapter.VenuesViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenuesViewHolder {
        context = parent.context
        return VenuesViewHolder(
            ItemVenuesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: VenuesViewHolder, position: Int) {
        holder.bind(context, venuesList[position])
    }

    override fun getItemCount(): Int {
        return venuesList.size
    }

    fun updateList(list: List<Venues>) {
        venuesList.clear()
        venuesList.addAll(list)
        this.notifyDataSetChanged()
    }

    fun getVenuesList(): List<Venues> {
        return venuesList
    }

    fun getVenuesAt(index: Int): Venues {
        return venuesList[index]
    }

    class VenuesViewHolder(private val binding: ItemVenuesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, venues: Venues) {
            binding.apply {
                tvName.text = venues.name
                tvAddress.text = context.getString(R.string.full_address, getAddress(venues))
                if (venues.categories.isNotEmpty()) {
                    tvCategory.text =
                        context.getString(R.string.category, venues.categories[0].name)
                    Glide.with(context)
                        .load(getIconURL(venues))
                        .into(ivCategory)

                    groupCategory.visibility = View.VISIBLE
                } else {
                    groupCategory.visibility = View.GONE
                }
            }
        }
    }
}