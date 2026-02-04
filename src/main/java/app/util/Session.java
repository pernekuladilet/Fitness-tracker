package app.model;

public class Session {
    public static User currentUser;

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
