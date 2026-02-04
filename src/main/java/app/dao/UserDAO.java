package app.dao;

import app.model.User;
import app.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username FROM public.users ORDER BY id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new User(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
