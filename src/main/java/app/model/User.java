package app.model;

public class User {
    private final int id;
    private final String username;
    private final String role; // ADMIN / USER

    // для списка пользователей (когда роль не нужна) — можно ставить USER по умолчанию
    public User(int id, String username) {
        this(id, username, "USER");
    }

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = (role == null || role.isBlank()) ? "USER" : role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
