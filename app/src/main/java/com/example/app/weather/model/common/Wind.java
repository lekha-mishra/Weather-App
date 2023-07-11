package com.example.app.weather.model.common;

import com.google.gson.annotations.SerializedName;

public class Wind {

  @SerializedName("deg")
  private double deg;

  @SerializedName("speed")
  private double speed;

  @SerializedName("gust")
  private double gust;

  public double getGust() {
    return gust;
  }

  public void setGust(double gust) {
    this.gust = gust;
  }

  public double getDeg() {
    return deg;
  }

  public void setDeg(double deg) {
    this.deg = deg;
  }

  public double getSpeed() {
    return speed;
  }

  public void setSpeed(double speed) {
    this.speed = speed;
  }

}