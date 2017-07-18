package com.example.android.app.myview.popularmovies;


public class Review {

    private String reviewAuthor;
    private String reviewContent;

    private String reviewURL;

    public Review(String reviewAuthor, String reviewContent, String reviewURL){
        this.setReviewAuthor(reviewAuthor);
        this.setReviewContent(reviewContent);
        this.setReviewURL(reviewURL);
    }


    public String getReviewAuthor() {
        return reviewAuthor;
    }

    public void setReviewAuthor(String reviewAuthor) {
        this.reviewAuthor = reviewAuthor;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public String getReviewURL() {
        return reviewURL;
    }

    public void setReviewURL(String reviewURL) {
        this.reviewURL = reviewURL;
    }

}
