package com.movieticket.gongding.dao;

import com.movieticket.gongding.entity.Movie;
import com.movieticket.gongding.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MovieDao {
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();

        String sql = "SELECT * FROM movies";
        try (Connection conn = JDBCUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                //添加查询内容到一个List中
                movies.add(mapMovieFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取电影列表失败: " + e.getMessage());
        }
        return movies;
    }

    private Movie mapMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setDescription(rs.getString("description"));
        movie.setShowtime(rs.getTimestamp("showtime").toLocalDateTime());
        movie.setDuration(rs.getInt("duration"));
        movie.setTotalSeats(rs.getInt("total_seats"));
        movie.setAvailableSeats(rs.getInt("available_seats"));
        movie.setVersion(rs.getInt("version"));
        movie.setPrice(rs.getBigDecimal("price"));
        return movie;
    }
}
