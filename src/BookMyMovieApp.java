import service.BookMyMoviesSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class BookMyMovieApp {
    public static void main(String[] args) {
        BookMyMoviesSystem sys = new BookMyMoviesSystem();

        Scanner sc = new Scanner(System.in);
        System.out.println("List of Current Users");
        sys.displayUsers();
        System.out.println("Select your User Id");
        int userId = sc.nextInt();
        sys.authenticateUser(userId);

        System.out.println("Enter 1 to Book. 0 to Cancel Booking");
        int choice = sc.nextInt();
        sc.nextLine();

        if(choice == 1) {
            System.out.println("Enter your city: ");
            String city = sc.nextLine();
            int movieId = sys.displayMovies(city);

            sys.displayTheaters(city, movieId);
            System.out.println("Select Theater No.: ");
            int theaterId = sc.nextInt();
            sys.displayShows(movieId, theaterId);

            System.out.println("Select the Show Id with which you want to proceed: ");
            int showId = sc.nextInt();
            HashSet<Integer> validSeats = sys.displayAvailableSeats(showId, theaterId);

            List<Integer> selectedSeatsId = sys.selectSeats(validSeats);
            List<String> selectedSeats = sys.findSeatsById(selectedSeatsId);

            if (selectedSeats == null || selectedSeats.isEmpty()) {
                System.out.println("No seats selected. Kindly start a new booking....");
                return;
            }

            System.out.println("Your Selected Seats: " + selectedSeats);
            sys.bookTicket(userId, showId, selectedSeats);

        } else if(choice == 0){
            sys.cancelBooking();
        } else{
            System.out.println("Invalid Selection");
        }
    }
}
