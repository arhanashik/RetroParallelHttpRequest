package com.blackspider.retroparallelhttprequest.data.model;
/*
 *  ****************************************************************************
 *  * Created by : Arhan Ashik on 12/3/2018 at 12:43 PM.
 *  * Email : ashik.pstu.cse@gmail.com
 *  *
 *  * Last edited by : Arhan Ashik on 12/3/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import com.google.gson.annotations.SerializedName;

public class Price {
    float price;
    String seats;
    String currency;

    @SerializedName("flight_number")
    String flightNumber;

    String from;
    String to;

    public float getPrice() {
        return price;
    }

    public String getSeats() {
        return seats;
    }

    public String getCurrency() {
        return currency;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
