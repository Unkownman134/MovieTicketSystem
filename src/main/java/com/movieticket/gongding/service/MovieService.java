package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.entity.Movie;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovieService {
    private MovieDao movieDao = new MovieDao();

    public boolean addMovie(String title, String description, LocalDateTime showtime, int duration, int totalSeats, String seats, BigDecimal price) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setShowtime(showtime);
        movie.setDuration(duration);
        movie.setTotalSeats(totalSeats);
        movie.setAvailableSeats(totalSeats);
        movie.setVersion(0);
        movie.setSeats(seats);
        movie.setPrice(price);

        try {
            return movieDao.addMovie(movie);
        } catch (Exception e) {
            return false;
        }
    }
}
