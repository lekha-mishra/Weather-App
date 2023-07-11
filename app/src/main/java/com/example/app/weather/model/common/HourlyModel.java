package com.example.app.weather.model.common;

public class HourlyModel {

  private  int id;
  private  String  timinghourly;
  private  int  rainstatus;
  private  String  rainpercent;
  private  String  raintemp;

    public HourlyModel(int id, String timinghourly, int rainstatus, String rainpercent, String raintemp) {
        this.id = id;
        this.timinghourly = timinghourly;
        this.rainstatus = rainstatus;
        this.rainpercent = rainpercent;
        this.raintemp = raintemp;
    }

    public int getRainstatus() {
        return rainstatus;
    }

    public void setRainstatus(int rainstatus) {
        this.rainstatus = rainstatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTiminghourly() {
        return timinghourly;
    }

    public void setTiminghourly(String timinghourly) {
        this.timinghourly = timinghourly;
    }



    public String getRainpercent() {
        return rainpercent;
    }

    public void setRainpercent(String rainpercent) {
        this.rainpercent = rainpercent;
    }

    public String getRaintemp() {
        return raintemp;
    }

    public void setRaintemp(String raintemp) {
        this.raintemp = raintemp;
    }
}
