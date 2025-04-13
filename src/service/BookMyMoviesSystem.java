package service;

import config.DatabaseConfig;

import java.sql.*;
import java.util.*;

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
            String query = """
                    SELECT s.seat_id, s.seat_number, s.seat_type \
                    FROM seats s JOIN shows ON shows.show_id = s.show_id \
                    WHERE s.show_id = ? AND is_booked = 0 AND shows.theater_id = ?""";

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
            System.out.printf("+----------+--------------+-------------+\n");
            System.out.printf("| %-8s | %-12s | %-11s |\n", "Seat ID", "Seat Number", "Seat Type");
            System.out.printf("+----------+--------------+-------------+\n");

            while (res.next()) {
                int seatId = res.getInt("seat_id");
                String seatNumber = res.getString("seat_number");
                String seatType = res.getString("seat_type");
                validSeats.add(seatId);
                System.out.printf("| %-8d | %-12s | %-11s |\n", seatId, seatNumber, seatType);
            }

            System.out.printf("+----------+--------------+-------------+\n");

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
        try {
            Connection conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            boolean allSeatsAvailable = true;
            double totalPrice = 0.0;
            List<String> unavailableSeats = new ArrayList<>();
            List<String> seatTypes = new ArrayList<>();

            for (String seat : selectedSeats) {
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT seat_type, is_booked FROM seats WHERE show_id = ? AND seat_number = ?"
                );
                checkStmt.setInt(1, showId);
                checkStmt.setString(2, seat);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    boolean isBooked = rs.getBoolean("is_booked");
                    String seatType = rs.getString("seat_type");

                    if (isBooked) {
                        allSeatsAvailable = false;
                        unavailableSeats.add(seat);
                    } else {
                        seatTypes.add(seatType);
                    }
                } else {
                    allSeatsAvailable = false;
                    unavailableSeats.add(seat + " (Invalid)");
                }
            }

            if (!allSeatsAvailable) {
                System.out.println("Booking failed. These seats are already booked or invalid: " + unavailableSeats);
                conn.rollback();
                return;
            }

            // Calculate total price
            for (String seatType : seatTypes) {
                PreparedStatement priceStmt = conn.prepareStatement(
                        "SELECT price FROM seat_pricing WHERE seat_type = ?"
                );
                priceStmt.setString(1, seatType);
                ResultSet rs = priceStmt.executeQuery();

                if (rs.next()) {
                    totalPrice += rs.getDouble("price");
                } else {
                    System.out.println("No pricing found for seat type: " + seatType);
                    conn.rollback();
                    return;
                }
            }

            // Update seats as booked
            for (String seat : selectedSeats) {
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE seats SET is_booked = true WHERE show_id = ? AND seat_number = ?"
                );
                updateStmt.setInt(1, showId);
                updateStmt.setString(2, seat);
                updateStmt.executeUpdate();
            }

            // Update shows table: decrease available seats
            PreparedStatement updateShowStmt = conn.prepareStatement(
                    "UPDATE shows SET available_seats = available_seats - ? WHERE show_id = ?"
            );
            updateShowStmt.setInt(1, selectedSeats.size());
            updateShowStmt.setInt(2, showId);
            updateShowStmt.executeUpdate();

            // Insert booking into bookings table
            PreparedStatement bookingStmt = conn.prepareStatement(
                    "INSERT INTO bookings (user_id, show_id, seats_booked, total_price) VALUES (?, ?, ?, ?)"
            );
            bookingStmt.setInt(1, userId);
            bookingStmt.setInt(2, showId);
            bookingStmt.setString(3, String.join(",", selectedSeats));
            bookingStmt.setDouble(4, totalPrice);
            bookingStmt.executeUpdate();

            conn.commit();
            System.out.println("🎟️ Booking Successful!");
            System.out.println("Seats: " + selectedSeats + " | Total Price: ₹" + totalPrice);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelBooking(int userId){
        try{
            String query = """
                    SELECT b.booking_id, b.seats_booked, b.total_price, b.booking_time, \
                    s.show_timing, m.title \
                    FROM bookings b \
                    JOIN shows s ON b.show_id = s.show_id \
                    JOIN movies m ON s.movie_id = m.movie_id \
                    WHERE b.user_id = ? AND b.booking_status = 'confirmed'""";

            Connection conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet res = stmt.executeQuery();
            if(!res.isBeforeFirst()){
                System.out.println("No active bookings for User Id: " + userId);
                return;
            }

            System.out.println("\n🎟️  Your Active Bookings:");
            System.out.printf("+------------+-------------------------+-------------------+---------------------+--------+---------------------+%n");
            System.out.printf("| Booking ID | Movie Title             | Seats             | Show Time           | Price  | Booking Time        |%n");
            System.out.printf("+------------+-------------------------+-------------------+---------------------+--------+---------------------+%n");


            while(res.next()){
                int bookingId = res.getInt("booking_id");
                String title = res.getString("title");
                String seats = res.getString("seats_booked");
                String showTime = res.getString("show_timing");
                double price = res.getDouble("total_price");
                Timestamp bookingTime = res.getTimestamp("booking_time");

                System.out.printf("| %-10d | %-23s | %-17s | %-19s | %-6.2f | %-19s |%n",
                        bookingId, title, seats, showTime, price, bookingTime.toString());
            }
            System.out.printf("+------------+-------------------------+-------------------+---------------------+--------+---------------------+%n");

            System.out.print("\nEnter the Booking ID to cancel: ");
            int cancelId = sc.nextInt();

            PreparedStatement cancelBooking = conn.prepareStatement("""
                    SELECT show_id, seats_booked FROM bookings WHERE booking_id = ? AND user_id = ? AND booking_status = 'confirmed'
                    """);

            cancelBooking.setInt(1, cancelId);
            cancelBooking.setInt(2, userId);

            ResultSet rs = cancelBooking.executeQuery();
            if (!rs.next()) {
                System.out.println("Invalid Booking ID or Booking already cancelled.");
                return;
            }

            int showId = rs.getInt("show_id");
            String[] seatsArray = rs.getString("seats_booked").split(",");

            for (String seat : seatsArray) {
                PreparedStatement unbookSeat = conn.prepareStatement(
                        "UPDATE seats SET is_booked = false WHERE show_id = ? AND seat_number = ?"
                );
                unbookSeat.setInt(1, showId);
                unbookSeat.setString(2, seat);
                unbookSeat.executeUpdate();
            }

            PreparedStatement updateShow = conn.prepareStatement(
                    "UPDATE shows SET available_seats = available_seats + ? WHERE show_id = ?"
            );
            updateShow.setInt(1, seatsArray.length);
            updateShow.setInt(2, showId);
            updateShow.executeUpdate();

            PreparedStatement result = conn.prepareStatement(
                    "UPDATE bookings SET booking_status = 'cancelled' WHERE booking_id = ?"
            );
            result.setInt(1, cancelId);
            result.executeUpdate();

            conn.commit();
            System.out.println("✅ Booking cancelled successfully! Freed seats: " + Arrays.toString(seatsArray));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
