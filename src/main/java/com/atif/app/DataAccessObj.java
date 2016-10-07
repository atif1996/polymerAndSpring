package com.atif.app;
import java.sql.*;
import java.util.Vector;


/**
 * Simple wrapper database access methods.  Decided to go basic sqlite.
 */
public class DataAccessObj
{
    private Connection m_con            = null;
    PreparedStatement m_deleteReq       = null;
    PreparedStatement m_hasUserReq      = null;
    PreparedStatement m_insertReq       = null;
    PreparedStatement m_selectAllReq    = null;
    PreparedStatement m_selectUserReq   = null;
    PreparedStatement m_updateReq       = null;
    DataAccessObj() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            m_con = DriverManager.getConnection("jdbc:sqlite::memory:");
            Statement sql = m_con.createStatement();
            sql.setQueryTimeout(30);
            sql.executeUpdate("CREATE table users (id INTEGER PRIMARY KEY, firstName TEXT, lastName TEXT, age integer)");
            m_deleteReq     = m_con.prepareStatement("DELETE FROM users WHERE id = ?");
            m_hasUserReq    = m_con.prepareStatement("SELECT id from users WHERE id = ? LIMIT 1");
            m_insertReq     = m_con.prepareStatement("INSERT INTO users (firstName, lastName, age) VALUES (?, ?, ?)");
            m_selectAllReq  = m_con.prepareStatement("SELECT id, firstName, lastName, age FROM users");
            m_selectUserReq = m_con.prepareStatement("SELECT id, firstName, lastName, age FROM users WHERE id = ? LIMIT 1");
            m_updateReq     = m_con.prepareStatement("UPDATE users SET firstName = ?, lastName = ?, age = ? WHERE id=? ");

        } catch(SQLException e) {
            System.err.println(e.getMessage());
            throw e;
        }

    }

    boolean hasUser(int id) throws SQLException {
        m_hasUserReq.setInt(1, id);
        ResultSet set = m_hasUserReq.executeQuery();
        try {
            if (set.next())
                return true;
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    UserRecord getUser(int id) throws SQLException {
        ResultSet set = m_selectAllReq.executeQuery();

        if(set.next()) {
            UserRecord rec = new UserRecord(set.getInt(1), set.getString(2), set.getString(3), set.getInt(4));
            return rec;
        }
        return null;
    }

    Vector<UserRecord> getUsers() throws SQLException {
        Vector<UserRecord> list = new Vector<>();
        ResultSet set = m_selectAllReq.executeQuery();

        while(set.next()) {
            UserRecord rec = new UserRecord(set.getInt(1), set.getString(2), set.getString(3), set.getInt(4));
            list.add(rec);
        }
        return list;
    }

    int insertUser(String first, String last, int age) throws SQLException {
        m_insertReq.setString(1, first);
        m_insertReq.setString(2, last);
        m_insertReq.setInt(3, age);
        int numRows = m_insertReq.executeUpdate();
        System.out.print( numRows );
        if (numRows == 0)
            return -1;
        else
            return m_insertReq.getGeneratedKeys().getInt(1);
    }

    boolean deleteUser(int id) throws SQLException {
        m_deleteReq.setInt(1, id);
        int resultSet = m_deleteReq.executeUpdate();
        return resultSet != 0;
    }

    boolean updateUser(UserRecord updatedUser) throws SQLException {
        m_updateReq.setString(1, updatedUser.firstName);
        m_updateReq.setString(2, updatedUser.lastName);
        m_updateReq.setInt(3, updatedUser.age);
        m_updateReq.setInt(4, updatedUser.rowId);
        int result = m_updateReq.executeUpdate();
        return result != 0;
    }
}
