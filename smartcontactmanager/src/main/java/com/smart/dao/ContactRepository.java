package com.smart.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

import antlr.collections.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	//pagination
	@Query("from Contact as c where c.user.id =:userId")
	public ArrayList<Contact> findContactsByUser(@Param("userId") int userId);
}
