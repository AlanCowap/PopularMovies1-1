package com.example.android.app.myview.popularmovies;



public class Trailer {


    private String trailerUri;
    private String trailerDesc;

    public Trailer(String trailerUri, String trailerDesc){
        super();
        this.setTrailerUri(trailerUri);
        this.setTrailerDesc(trailerDesc);
    }
    public String getTrailerUri() {
        return trailerUri;
    }

    public void setTrailerUri(String trailerUri) {
        this.trailerUri = trailerUri;
    }

    public String getTrailerDesc() {
        return trailerDesc;
    }

    public void setTrailerDesc(String trailerDesc) {
        this.trailerDesc = trailerDesc;
    }
}
