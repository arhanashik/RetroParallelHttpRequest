package com.blackspider.retroparallelhttprequest.ui.main.adapter;
/*
 *  ****************************************************************************
 *  * Created by : Arhan Ashik on 12/3/2018 at 12:48 PM.
 *  * Email : ashik.pstu.cse@gmail.com
 *  *
 *  * Last edited by : Arhan Ashik on 12/3/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackspider.retroparallelhttprequest.R;
import com.blackspider.retroparallelhttprequest.data.model.Ticket;
import com.blackspider.retroparallelhttprequest.databinding.TicketRowBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Locale;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.MyViewHolder> {
    private List<Ticket> ticketList;
    private TicketsAdapterListener listener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TicketRowBinding mBinding;

        MyViewHolder(TicketRowBinding binding) {
            super(binding.getRoot());

            mBinding = binding;

            mBinding.getRoot().setOnClickListener(view -> {
                // send selected contact in callback
                listener.onTicketSelected(ticketList.get(getAdapterPosition()));
            });
        }

        void bind(Ticket ticket) {
            Glide.with(mBinding.getRoot().getContext())
                    .load(ticket.getAirline().getLogo())
                    .apply(RequestOptions.circleCropTransform())
                    .into(mBinding.logo);

            mBinding.airlineName.setText(ticket.getAirline().getName());

            String departure = ticket.getDeparture() + " Dep";
            mBinding.departure.setText(departure);
            String arrival = ticket.getArrival() + " Dest";
            mBinding.arrival.setText(arrival);

            mBinding.duration.setText(ticket.getFlightNumber());
            mBinding.duration.append(", " + ticket.getDuration());
            String stops = ticket.getNumberOfStops() + " Stops";
            mBinding.numberOfStops.setText(stops);

            if (!TextUtils.isEmpty(ticket.getInstructions())) {
                mBinding.duration.append(", " + ticket.getInstructions());
            }

            if (ticket.getPrice() != null) {
                String price = "₹" + String.format(Locale.ENGLISH,
                        "%.0f", ticket.getPrice().getPrice());
                mBinding.price.setText(price);
                String seats = ticket.getPrice().getSeats() + " Seats";
                mBinding.numberOfSeats.setText(seats);
                mBinding.loader.setVisibility(View.INVISIBLE);
            } else {
                mBinding.loader.setVisibility(View.VISIBLE);
            }
        }
    }

    public TicketsAdapter(List<Ticket> ticketList, TicketsAdapterListener listener) {
        this.listener = listener;
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TicketRowBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.ticket_row,
                parent,
                false);

        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.bind(ticketList.get(position));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public interface TicketsAdapterListener {
        void onTicketSelected(Ticket ticket);
    }
}
