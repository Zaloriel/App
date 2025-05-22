package dao.impl;

import dao.UserDao;
import models.User;
import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserDao interface
 */
public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.debug("User saved successfully: {}", user);
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            logger.debug("Found user by id {}: {}", id, user);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> rootEntry = cq.from(User.class);
            CriteriaQuery<User> all = cq.select(rootEntry);
            Query<User> allQuery = session.createQuery(all);
            List<User> users = allQuery.getResultList();
            logger.debug("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error finding all users: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding all users", e);
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            logger.debug("User updated successfully: {}", user);
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public void delete(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(user);
            transaction.commit();
            logger.debug("User deleted successfully: {}", user);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Optional<User> user = findById(id);
            if (user.isPresent()) {
                transaction = session.beginTransaction();
                session.remove(user.get());
                transaction.commit();
                logger.debug("User with id {} deleted successfully", id);
                return true;
            } else {
                logger.debug("User with id {} not found for deletion", id);
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error deleting user by id", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root).where(cb.equal(root.get("email"), email));
            Query<User> query = session.createQuery(cq);
            User user = query.uniqueResult();
            logger.debug("Found user by email {}: {}", email, user);
            return user;
        } catch (Exception e) {
            logger.error("Error finding user by email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error finding user by email", e);
        }
    }
}