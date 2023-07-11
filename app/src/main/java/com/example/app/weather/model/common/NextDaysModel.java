package com.example.app.weather.model.common;

public class NextDaysModel {
    private  int id;
    private  String  avgTemp;
    private  int  rainTypeImg;
    private  String  highTemp;
    private  String  lowTemp;
    private  String  dayandDate;
    private  String  rainType;

    public NextDaysModel(int id, String avgTemp, int rainTypeImg, String highTemp, String lowTemp, String dayandDate, String rainType) {
        this.id = id;
        this.avgTemp = avgTemp;
        this.rainTypeImg = rainTypeImg;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.dayandDate = dayandDate;
        this.rainType = rainType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvgTemp() {
        return avgTemp;
    }

    public void setAvgTemp(String avgTemp) {
        this.avgTemp = avgTemp;
    }

    public int getRainTypeImg() {
        return rainTypeImg;
    }

    public void setRainTypeImg(int rainTypeImg) {
        this.rainTypeImg = rainTypeImg;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public String getDayandDate() {
        return dayandDate;
    }

    public void setDayandDate(String dayandDate) {
        this.dayandDate = dayandDate;
    }

    public String getRainType() {
        return rainType;
    }

    public void setRainType(String rainType) {
        this.rainType = rainType;
    }
}
