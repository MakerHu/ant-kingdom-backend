package com.springboot.springbootlogindemo.repository;

import com.springboot.springbootlogindemo.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoomDao extends JpaRepository<Room, Long> {
    Room findById(String id);

}
