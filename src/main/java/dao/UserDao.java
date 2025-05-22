package dao;

import models.User;


public interface UserDao extends GenericDao<User, Long> {


    User findByEmail(String email);
}