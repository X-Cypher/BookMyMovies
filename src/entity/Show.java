package entity;

public class Show {
    private int showId;
    private int movieId;
    private int theaterId;
    private int showTiming;
    private int availableSeats;

    public Show(int showId, int movieId, int theaterId, int showTiming, int availableSeats) {
        this.showId = showId;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.showTiming = showTiming;
        this.availableSeats = availableSeats;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(int theaterId) {
        this.theaterId = theaterId;
    }

    public int getShowTiming() {
        return showTiming;
    }

    public void setShowTiming(int showTiming) {
        this.showTiming = showTiming;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
