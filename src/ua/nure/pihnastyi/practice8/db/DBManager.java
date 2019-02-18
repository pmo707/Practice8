package ua.nure.pihnastyi.practice8.db;


import ua.nure.pihnastyi.practice8.Fields;
import ua.nure.pihnastyi.practice8.db.entity.Team;
import ua.nure.pihnastyi.practice8.db.entity.User;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBManager {


   // private static final String CONNECTION_URL = props.getProperty("connection.url");


    private static final String SQL_FIND_ALL_USERS =
            "SELECT * FROM users";
    private static final String SQL_FIND_ALL_TEAMS =
            "SELECT * FROM teams";
    private static final String SQL_CREATE_USER = "INSERT INTO users VALUES (default, ?)";
    private static final String SQL_CREATE_TEAMS = "INSERT INTO teams VALUES (default, ?)";
    private static final String SQL_FIND_USER_BY_LOGIN =
            "SELECT * FROM users WHERE login=?";
    private static final String SQL_FIND_TEAM_BY_NAME =
            "SELECT * FROM teams WHERE name=?";
    private static final String SQL_DELETE_TEAM =
            "DELETE FROM teams WHERE id=?";

    private static final String SQL_UPDATE_TEAM =
            "UPDATE teams  SET teams.name =? WHERE teams.id=?";


    private static DBManager instance;

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    private DBManager() {
    }

    public User getUser(String userLogin) {
        User user = null;

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(SQL_FIND_USER_BY_LOGIN);
            pstmt.setString(1, userLogin);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                user = extractUser(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
        return user;
    }

    public Team getTeam(String teamName) {

        Team team = null;

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(SQL_FIND_TEAM_BY_NAME);
            pstmt.setString(1, teamName);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                team = extractTeam(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
        return team;
    }

    public void insertUser(User user) throws SQLException {

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(SQL_CREATE_USER,
                    Statement.RETURN_GENERATED_KEYS);
            int k = 1;
            pstmt.setString(k++, user.getLogin());


            if (pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    user.setId(userId);

                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();


        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
    }

    public void insertTeam(Team team) throws SQLException {

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(SQL_CREATE_TEAMS,
                    Statement.RETURN_GENERATED_KEYS);
            int k = 1;
            pstmt.setString(k++, team.getName());


            if (pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int teamId = rs.getInt(1);
                    team.setId(teamId);

                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();


        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
    }

    public List<User> findAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(SQL_FIND_ALL_USERS);
            while (rs.next()) {
                users.add(extractUser(rs));
            }


        } finally {
            close(rs);
            close(stmt);
            close(connection);
        }
        return users;
    }

    public List<Team> findAllTeams() throws SQLException {
        List<Team> teams = new ArrayList<>();

        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(SQL_FIND_ALL_TEAMS);
            while (rs.next()) {
                teams.add(extractTeam(rs));
            }


        } finally {
            close(rs);
            close(stmt);
            close(connection);
        }
        return teams;
    }


    public Connection getConnection() throws SQLException {
        Properties props = new Properties();
        FileInputStream in;

        {
            try {
                in = new FileInputStream("app.properties");
                props.load(in);
                in.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String url = props.getProperty("connection.url");
        Connection connection = DriverManager.getConnection(url);
        return connection;
    }

    private static User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt(Fields.USER_ID));

        user.setLogin(rs.getString(Fields.USER_LOGIN));
        return user;
    }

    private static Team extractTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getInt(Fields.TEAM_ID));
        team.setName(rs.getString(Fields.TEAM_NAME));
        return team;
    }

    public static void close(AutoCloseable ac) {
        if (ac != null) {
            try {
                ac.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void setTeamsForUser(User userLogin, Team... teamName) throws SQLException {

        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (Team team : teamName) {
            if (team == null) return;
            ps = connection.prepareStatement("SELECT * FROM users_teams WHERE user_id=? AND team_id=?");
            ps.setInt(1, userLogin.getId());
            ps.setInt(2, team.getId());
            rs = ps.executeQuery();
        }

        connection.setAutoCommit(false);
        if (!rs.next()) {
            try {
                for (Team team : teamName) {
                    ps = connection.prepareStatement("INSERT INTO users_teams VALUES(?, ?)");
                    ps.setInt(1, userLogin.getId());
                    ps.setInt(2, team.getId());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException();
            } finally {
                rs.close();
                ps.close();
                connection.close();
            }
        }
        rs.close();
        ps.close();
        connection.close();

    }

    public List<Team> getUserTeams(User user) throws SQLException {
        List<Team> teams = new ArrayList<>();
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = connection.prepareStatement(
                "SELECT teams.id, name\n" +
                        "FROM teams,\n" +
                        "     users_teams,\n" +
                        "     users\n" +
                        "WHERE user_id = ?\n" +
                        "  AND user_id = users.id\n" +
                        "  AND team_id = teams.id\n" +
                        "order by teams.id");
        ps.setInt(1, user.getId());
        rs = ps.executeQuery();

        while (rs.next()) {
            Team team = Team.createTeam(rs.getString("name"));
            team.setId(rs.getInt("id"));
            teams.add(team);
        }
        rs.close();
        ps.close();
        connection.close();
        return teams;
    }

    public boolean deleteTeam(Team teamName) {

 boolean res= false;
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(SQL_DELETE_TEAM);
            pstmt.setInt(1, teamName.getId());
            System.out.println("--------------------------------");
            res= pstmt.executeUpdate() > 0;

        } catch (SQLException ex) {

            ex.printStackTrace();

        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
        return res;
    }

    public boolean updateTeam(Team teamName) {
        boolean res= false;
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(SQL_UPDATE_TEAM);
            pstmt.setString(1, teamName.getName());
            pstmt.setInt(2, teamName.getId());
            res = pstmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            // (1) write to log
            // LOGGER.error("Cannot delete user with id: " + userId, ex);
            ex.printStackTrace();

        } finally {
            close(rs);
            close(pstmt);
            close(connection);
        }
        return res;
    }
}
