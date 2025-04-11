import service.BookMyMoviesSystem;

import java.util.Scanner;

public class BookMyMovieApp {
    public static void main(String[] args) {
        BookMyMoviesSystem sys = new BookMyMoviesSystem();

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your city: ");
        String city = sc.nextLine();
        int movieId = sys.displayMovies(city);

        if(movieId == -1 || !sys.displayTheaters(city, movieId)){ //no theater found. Exit
            return;
        }

        System.out.println("Select Theater No.: ");
        int theaterId = sc.nextInt();
        sys.displayShows(movieId, theaterId);
    }
}
