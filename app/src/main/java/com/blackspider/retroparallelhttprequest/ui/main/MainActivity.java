package com.blackspider.retroparallelhttprequest.ui.main;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blackspider.retroparallelhttprequest.R;
import com.blackspider.retroparallelhttprequest.data.model.Ticket;
import com.blackspider.retroparallelhttprequest.databinding.ActivityMainBinding;
import com.blackspider.retroparallelhttprequest.ui.main.adapter.TicketsAdapter;
import com.blackspider.util.helper.Converter;
import com.blackspider.util.helper.GridSpacingItemDecoration;
import com.blackspider.util.lib.network.ApiClient;
import com.blackspider.util.lib.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TicketsAdapter.TicketsAdapterListener{

    private ActivityMainBinding mBinding;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String from = "DEL";
    private static final String to = "HYD";

    private CompositeDisposable disposable = new CompositeDisposable();

    private ApiService apiService;
    private TicketsAdapter mAdapter;
    private ArrayList<Ticket> ticketsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setTitle(from + " > " + to);

        apiService = ApiClient.getClient().create(ApiService.class);

        mAdapter = new TicketsAdapter(ticketsList, this);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mBinding.recyclerView.setLayoutManager(mLayoutManager);
        mBinding.recyclerView.addItemDecoration(new GridSpacingItemDecoration(1,
                Converter.dpToPx(this, 5), true));
        mBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mBinding.recyclerView.setAdapter(mAdapter);

        ConnectableObservable<List<Ticket>> ticketsObservable = getTickets(from, to).replay();

        /**
         * Fetching all tickets first
         * Observable emits List<Ticket> at once
         * All the items will be added to RecyclerView
         * */
        disposable.add(
                ticketsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<List<Ticket>>() {

                            @Override
                            public void onNext(List<Ticket> tickets) {
                                // Refreshing list
                                ticketsList.clear();
                                ticketsList.addAll(tickets);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                showError(e);
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

        /**
         * Fetching individual ticket price
         * First FlatMap converts single List<Ticket> to multiple emissions
         * Second FlatMap makes HTTP call on each Ticket emission
         * */
        disposable.add(
                ticketsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        /**
                         * Converting List<Ticket> emission to single Ticket emissions
                         * concatMap -> executes HTTP calls sequentially
                         * flatMap -> executes HTTP calls parallel
                         * */
                        //.concatMap((Function<List<Ticket>, ObservableSource<Ticket>>)
                        //                Observable::fromIterable)
                        .flatMap((Function<List<Ticket>, ObservableSource<Ticket>>)
                                Observable::fromIterable)
                        /**
                         * Fetching price on each Ticket emission
                         * */
                        //.concatMap((Function<Ticket, ObservableSource<Ticket>>)
                        //        this::getPriceObservable)
                        .flatMap((Function<Ticket, ObservableSource<Ticket>>)
                               this::getPriceObservable)
                        .subscribeWith(new DisposableObserver<Ticket>() {

                            @Override
                            public void onNext(Ticket ticket) {
                                int position = ticketsList.indexOf(ticket);

                                if (position == -1) {
                                    // Ticket not found in the list
                                    // This shouldn't happen
                                    return;
                                }

                                ticketsList.set(position, ticket);
                                mAdapter.notifyItemChanged(position);
                            }

                            @Override
                            public void onError(Throwable e) {
                                showError(e);
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

        // Calling connect to start emission
        ticketsObservable.connect();
    }

    /**
     * Making Retrofit call to fetch all tickets
     */
    private Observable<List<Ticket>> getTickets(String from, String to) {
        return apiService.searchTickets(from, to)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Making Retrofit call to get single ticket price
     * get price HTTP call returns Price object, but
     * map() operator is used to change the return type to Ticket
     */
    private Observable<Ticket> getPriceObservable(final Ticket ticket) {
        return apiService
                .getPrice(ticket.getFlightNumber(), ticket.getFrom(), ticket.getTo())
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(price -> {
                    ticket.setPrice(price);
                    return ticket;
                });
    }

    @Override
    public void onTicketSelected(Ticket ticket) {
        String msg = "Price still loading...";
        if(ticket.getPrice() != null)
            msg ="Book now only for ₹" + String.format(Locale.ENGLISH,
                    "%.0f", ticket.getPrice().getPrice());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showError(Throwable e) {
        Log.e(TAG, "showError: " + e.getMessage());

        Snackbar snackbar = Snackbar
                .make(mBinding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
