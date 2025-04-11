package service;

import config.DatabaseConfig;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class BookMyMoviesSystem {
    Scanner sc = new Scanner(System.in);

    public void displayMovies(){
        try{
            String query = "select * from movies";
            Connection conn = DatabaseConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(query);
            System.out.println("------------Available Movies------------");

            while(res.next()){
                System.out.println(res.getInt("movie_id") + ". " + res.getString("title") + " (" + res.getString("genre") + ")");
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void displayTheaters(String city){
        try{
            String query = "select * from theaters where city = ?";
            Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, city);
            ResultSet res = stmt.executeQuery();
            if(res == null){
                System.out.println("No Theaters found in " + city);
                return;
            }
            System.out.print("Theaters in " + city + ": ");
            int i = 1;
            while(res.next()){
                System.out.print(i++ + ". " + res.getString("name"));
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

            System.out.println("Available Shows: ");
            int i = 1;
            while (res.next()){
                System.out.println(i++ + ". " + res.getString("show_timing") + "- total seats available: " + res.getInt("available_seats"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void bookTicket(int userId, int showId, List<String> selectedSeats){
        try{
            Connection conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); //transaction pura hoga ya toh bilkul nhi hoga, partial statements execute nhi honge

            //check if the selectedSeats are available
            boolean selectedSeatsAreAvailable = true;
            for(String seat: selectedSeats){
                PreparedStatement stmt = conn.prepareStatement("select * from seats where show_id = ? and seat_number = ?");
                stmt.setInt(1, showId);
                stmt.setString(2, seat);
                ResultSet res = stmt.executeQuery();
                if(res.next() && res.getBoolean("is_booked")){
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
}
