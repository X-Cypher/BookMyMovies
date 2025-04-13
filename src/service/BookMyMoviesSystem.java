package service;

import config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class BookMyMoviesSystem {
    Scanner sc = new Scanner(System.in);

    public void displayUsers(){
        try{
            String query = "select * from users";
            Connection conn = DatabaseConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(query);
            if (!res.isBeforeFirst()) {
                System.out.println("No users found.");
                return;
            }

            System.out.printf("+----------+------------------------+-------------------------------+---------------+\n");
            System.out.printf("| %-8s | %-22s | %-29s | %-13s |\n", "User ID", "Name", "Email", "Phone");
            System.out.printf("+----------+------------------------+-------------------------------+---------------+\n");
            while (res.next()) {
                int userId = res.getInt("user_id");
                String name = res.getString("name");
                String email = res.getString("email");
                String phone = res.getString("phone");

                System.out.printf("| %-8d | %-22s | %-29s | %-13s |\n", userId, name, email, phone);
            }
            System.out.printf("+----------+------------------------+-------------------------------+---------------+\n");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void authenticateUser(int userId){
        try{
            String query = "select * from users where user_id = ?";
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet res = stmt.executeQuery();
            if(!res.isBeforeFirst()){
                System.out.println("Invalid User...");
                System.exit(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int displayMovies(String city){
        try{
            String query = """
                    SELECT DISTINCT m.movie_id, m.title, m.lang, m.genre
                    FROM movies m
                    JOIN shows s ON m.movie_id = s.movie_id
                    JOIN theaters t ON s.theater_id = t.theater_id
                    WHERE t.city = ? ORDER BY m.movie_id ASC;
                    """;

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, city);
            ResultSet res = stmt.executeQuery();

            if(!res.isBeforeFirst()){
                System.out.println("No movies available in " + city);
                System.exit(0);
            }

            System.out.println("Available Movies");
            System.out.printf("+------+------------------------------+------------+-----------------+\n");
            System.out.printf("| %-4s | %-28s | %-10s | %-15s |\n", "ID", "Title", "Language", "Genre");
            System.out.printf("+------+------------------------------+------------+-----------------+\n");

            while (res.next()) {
                int movieId = res.getInt("movie_id");
                String title = res.getString("title");
                String lang = res.getString("lang");
                String genre = res.getString("genre");

                System.out.printf("| %-4d | %-28s | %-10s | %-15s |\n", movieId, title, lang, genre);
            }

            System.out.printf("+------+------------------------------+------------+-----------------+\n");

            System.out.println("Choose Movie: ");
            int movieId = sc.nextInt();
            return movieId;

        } catch(SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public void displayTheaters(String city, int movieId){
        try{
            String query = """
                    SELECT DISTINCT\s
                        t.theater_id,
                        t.name AS theater_name,
                        s.show_timing,
                        s.available_seats,
                        m.lang AS movie_language\s
                    FROM theaters t
                    JOIN shows s ON t.theater_id = s.theater_id
                    JOIN movies m ON s.movie_id = m.movie_id
                    WHERE s.movie_id = ? AND t.city = ?;
                    """;

            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, movieId);
            stmt.setString(2, city);
            ResultSet res = stmt.executeQuery();

            if(!res.isBeforeFirst()){ //no theaters found
                PreparedStatement st = conn.prepareStatement("select title from movies where movie_id = ?");
                st.setInt(1, movieId);
                ResultSet result = st.executeQuery();

                String movieName = null;
                if(result.next()){
                    movieName = result.getString("title");
                }
                if(movieName == null){
                    System.out.println("Invalid Selection...");
                } else{
                    System.out.println("No Theaters found in " + city + " showing " + movieName);
                }
                System.exit(0);
            }

            System.out.printf("%-20s %-20s %-20s %-18s %-15s\n",
                    "Theater_No.","Theater", "Show Time", "Available Seats", "Language");
            System.out.println("--------------------------------------------------------------------------------------------");

            while (res.next()) {
                String theaterNo = res.getString("theater_id");
                String theaterName = res.getString("theater_name");
                String showTiming = res.getString("show_timing");
                int availableSeats = res.getInt("available_seats");
                String language = res.getString("movie_language");

                System.out.printf("%-20s %-20s %-20s %-18d %-15s\n",
                        theaterNo, theaterName, showTiming, availableSeats, language);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayShows(int movieId, int theaterId){
        try{
            String query = "select * from shows where movie_id = ? and theater_id = ?";
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, movieId);
            stmt.setInt(2, theaterId);
            ResultSet res = stmt.executeQuery();

            if(!res.isBeforeFirst()){
                System.out.println("Invalid Theater Selection...");
                System.exit(0);
            }

            System.out.println("Available Shows");
            System.out.printf("+---------+---------------------+----------------------+\n");
            System.out.printf("| %-7s | %-19s | %-20s |\n", "Show ID", "Show Timing", "Available Seats");
            System.out.printf("+---------+---------------------+----------------------+\n");

            while (res.next()) {
                int showId = res.getInt("show_id");
                String showTiming = res.getString("show_timing");
                int availableSeats = res.getInt("available_seats");

                System.out.printf("| %-7d | %-19s | %-20d |\n", showId, showTiming, availableSeats);
            }

            System.out.printf("+---------+---------------------+----------------------+\n");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashSet<Integer> displayAvailableSeats(int showId, int theaterId){
        HashSet<Integer> validSeats = new HashSet<>();
        try {
            String query = "SELECT s.seat_id, s.seat_number FROM seats s JOIN shows ON shows.show_id = s.show_id WHERE s.show_id = ? AND is_booked = 0 AND shows.theater_id = ?";
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, showId);
            stmt.setInt(2, theaterId);
            ResultSet res = stmt.executeQuery();

            if (!res.isBeforeFirst()) {
                System.out.println("No seats available for this show.");
                System.exit(0);
            }

            System.out.println("Available Seats:");
            System.out.printf("+----------+--------------+\n");
            System.out.printf("| %-8s | %-12s |\n", "Seat ID", "Seat Number");
            System.out.printf("+----------+--------------+\n");

            while (res.next()) {
                int seatId = res.getInt("seat_id");
                String seatNumber = res.getString("seat_number");
                validSeats.add(seatId);
                System.out.printf("| %-8d | %-12s |\n", seatId, seatNumber);
            }

            System.out.printf("+----------+--------------+\n");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return validSeats;
    }

    public List<Integer> selectSeats(HashSet<Integer> validSeats){
        List<Integer> selectedSeatsById = new ArrayList<>();
        do{
            System.out.println("Select the next Seat Id. Press 0 when you're ready to book.");
            int currSeat = sc.nextInt();
            if(currSeat == 0){
                return selectedSeatsById;
            }
            if(!validSeats.contains(currSeat)){
                System.out.println("Enter a valid seat Id");
                continue;
            }
            if(selectedSeatsById.contains(currSeat)){
                continue;
            }
            selectedSeatsById.add(currSeat);
        } while(true);
    }

    public List<String> findSeatsById(List<Integer> seats){
        List<String> seatNumbers = new ArrayList<String>();

        if (seats == null || seats.isEmpty()) {
            return seatNumbers;
        }

        try {
            Connection conn = DatabaseConfig.getConnection();
            String query = "SELECT seat_number FROM seats WHERE seat_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            for (int seatId : seats) {
                stmt.setInt(1, seatId);
                ResultSet res = stmt.executeQuery();
                if (res.next()) {
                    seatNumbers.add(res.getString("seat_number"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seatNumbers;
    }

    public void bookTicket(int userId, int showId, List<String> selectedSeats){
        try{
            Connection conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); //transaction pura hoga ya toh bilkul nhi hoga, partial statements execute nhi honge

            //check if the selectedSeats are available
            boolean selectedSeatsAreAvailable = true;
            for(String seat: selectedSeats){
                PreparedStatement stmt = conn.prepareStatement("select 1 from seats where show_id = ? and seat_number = ? and is_booked = true");
                stmt.setInt(1, showId);
                stmt.setString(2, seat);
                ResultSet res = stmt.executeQuery();

                if(res.next()){
                    selectedSeatsAreAvailable = false;
                    System.out.println("seat: " + seat + "is already booked. Choose another seat");
                }
            }

            // koi seat pehle se booked hai, toh booking rollback kr do
            if(!selectedSeatsAreAvailable){
                System.out.println("Booking failed. Some seats are already Booked");
                conn.rollback();
                return;
            }

            for(String seat: selectedSeats){
                PreparedStatement stmt = conn.prepareStatement("update seats set is_booked = true where show_id = ? and seat_number = ?");
                stmt.setInt(1, showId);
                stmt.setString(2, seat);
                stmt.executeUpdate();
            }
            double seatPrice = 200.0;
            double totalPrice = selectedSeats.size() * seatPrice;

            PreparedStatement stmt = conn.prepareStatement("insert into bookings (user_id, show_id, seats_booked, total_price) values (?,?,?,?)");
            stmt.setInt(1, userId);
            stmt.setInt(2, showId);
            stmt.setString(3, String.join(",", selectedSeats));
            stmt.setDouble(4, totalPrice);
            stmt.executeUpdate();
            conn.commit();
            System.out.println("Booking Successful, Seats: " + selectedSeats + " | Total Price: " + totalPrice);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelBooking(){

    }
}
