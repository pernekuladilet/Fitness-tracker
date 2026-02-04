package app.util;

import app.model.ExerciseCategory;
import app.model.TypeStat;
import app.model.User;
import app.model.Workout;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/MCM";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "170507";

    // ===================== INIT / CONNECTION =====================

    public static void initDatabase() {
        try (Connection conn = getConnection()) {
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ===================== AUTH =====================

    public static User login(String username, String password) {
        String sql = "SELECT id, username, role FROM public.users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, sha256(password));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===================== USERS =====================

    public static boolean addUser(String username, String password) {
        return addUser(username, password, "USER");
    }

    public static boolean addUser(String username, String password, String role) {
        String sql = "INSERT INTO public.users(username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, sha256(password));
            ps.setString(3, (role == null || role.isBlank()) ? "USER" : role.toUpperCase());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<User> getAllUsers() {
        String sql = "SELECT id, username, role FROM public.users ORDER BY id ASC";
        List<User> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean deleteUserById(int id) {
        String sql = "DELETE FROM public.users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== CATEGORIES =====================

    public static List<ExerciseCategory> getAllCategories() {
        String sql = "SELECT id, name FROM public.categories ORDER BY name ASC";
        List<ExerciseCategory> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ExerciseCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addCategory(String name) {
        String sql = "INSERT INTO public.categories(name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCategory(int id, String name) {
        String sql = "UPDATE public.categories SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCategoryById(int id) {
        String sql = "DELETE FROM public.categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== WORKOUTS =====================
    // ✅ ВАЖНО: метод объявлен ОДИН РАЗ (без дублей)

    public static List<Workout> getWorkoutsByUserId(int userId, LocalDate from, LocalDate to) {
        List<Workout> list = new ArrayList<>();

        String sql =
                "SELECT w.id, w.user_id, w.\"date\", w.type, w.duration_minutes, w.calories, w.notes, " +
                        "       w.category_id, c.name AS category_name " +
                        "FROM public.workouts w " +
                        "LEFT JOIN public.categories c ON c.id = w.category_id " +
                        "WHERE w.user_id = ? " +
                        "  AND (? IS NULL OR w.\"date\" >= ?) " +
                        "  AND (? IS NULL OR w.\"date\" <= ?) " +
                        "ORDER BY w.\"date\" DESC, w.id DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            if (from == null) {
                ps.setNull(2, Types.DATE);
                ps.setNull(3, Types.DATE);
            } else {
                Date f = Date.valueOf(from);
                ps.setDate(2, f);
                ps.setDate(3, f);
            }

            if (to == null) {
                ps.setNull(4, Types.DATE);
                ps.setNull(5, Types.DATE);
            } else {
                Date t = Date.valueOf(to);
                ps.setDate(4, t);
                ps.setDate(5, t);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Integer categoryId = (Integer) rs.getObject("category_id");
                    String categoryName = rs.getString("category_name");

                    list.add(new Workout(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getInt("duration_minutes"),
                            rs.getInt("calories"),
                            rs.getString("notes"),
                            categoryId,
                            categoryName
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public static boolean addWorkout(int userId, LocalDate date, String type,
                                     int durationMinutes, int calories, String notes,
                                     Integer categoryId) {

        String sql = "INSERT INTO public.workouts(user_id, \"date\", type, duration_minutes, calories, notes, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, type);
            ps.setInt(4, durationMinutes);
            ps.setInt(5, calories);
            ps.setString(6, notes);

            if (categoryId == null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, categoryId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean updateWorkout(int id, LocalDate date, String type,
                                        int durationMinutes, int calories, String notes,
                                        Integer categoryId) {

        String sql = "UP    DATE public.workouts " +
                "SET \"date\" = ?, type = ?, duration_minutes = ?, calories = ?, notes = ?, category_id = ? " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, type);
            ps.setInt(3, durationMinutes);
            ps.setInt(4, calories);
            ps.setString(5, notes);

            if (categoryId == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, categoryId);

            ps.setInt(7, id);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean deleteWorkoutById(int id) {
        String sql = "DELETE FROM public.workouts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== STATS BY TYPE =====================

    public static List<TypeStat> getWorkoutTypeStats(int userId, LocalDate from, LocalDate to) {
        List<TypeStat> list = new ArrayList<>();

        String sql =
                "SELECT type, COUNT(*) AS cnt, COALESCE(SUM(duration_minutes),0) AS minutes, COALESCE(SUM(calories),0) AS calories " +
                        "FROM public.workouts " +
                        "WHERE user_id = ? " +
                        "  AND (? IS NULL OR \"date\" >= ?) " +
                        "  AND (? IS NULL OR \"date\" <= ?) " +
                        "GROUP BY type " +
                        "ORDER BY cnt DESC, type ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            if (from == null) {
                ps.setNull(2, Types.DATE);
                ps.setNull(3, Types.DATE);
            } else {
                Date f = Date.valueOf(from);
                ps.setDate(2, f);
                ps.setDate(3, f);
            }

            if (to == null) {
                ps.setNull(4, Types.DATE);
                ps.setNull(5, Types.DATE);
            } else {
                Date t = Date.valueOf(to);
                ps.setDate(4, t);
                ps.setDate(5, t);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TypeStat(
                            rs.getString("type"),
                            rs.getInt("cnt"),
                            rs.getInt("minutes"),
                            rs.getInt("calories")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== CSV IMPORT =====================

    public static int importWorkoutsFromCsv(int userId, File file) {
        if (file == null) return 0;

        String sql = "INSERT INTO public.workouts(user_id, \"date\", type, duration_minutes, calories, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        int inserted = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (first) {
                    first = false;
                    String low = line.toLowerCase();
                    if (low.contains("date") && low.contains("type")) continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;

                LocalDate date = LocalDate.parse(parts[0].trim());
                String type = parts[1].trim();
                int duration = Integer.parseInt(parts[2].trim());
                int calories = Integer.parseInt(parts[3].trim());
                String notes = (parts.length >= 5) ? parts[4].trim() : "";

                ps.setInt(1, userId);
                ps.setDate(2, Date.valueOf(date));
                ps.setString(3, type);
                ps.setInt(4, duration);
                ps.setInt(5, calories);
                ps.setString(6, notes);

                ps.addBatch();
            }

            int[] res = ps.executeBatch();
            for (int r : res) if (r > 0) inserted += r;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return inserted;
    }

    // ===================== HASH =====================

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
