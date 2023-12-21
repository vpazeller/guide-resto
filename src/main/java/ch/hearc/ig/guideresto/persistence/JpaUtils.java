package ch.hearc.ig.guideresto.persistence;

import javax.persistence.*;
import java.util.function.Consumer;

public class JpaUtils {

    private static EntityManagerFactory emf;
    /**
     * Since we are developing a CLI application, there is no risk of concurrency
     * and the entity manager is thread safe.
     * If we have a threaded web application, it's best to use ThreadLocal
     */
    private static ThreadLocal<EntityManager> threadEm = new ThreadLocal<>();

    public static EntityManager getEntityManager() {
        EntityManager em = threadEm.get();
        if (em == null || !em.isOpen()) {
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory("guideRestoPersistenceUnit");
            }
            em = emf.createEntityManager();
            em.setFlushMode(FlushModeType.COMMIT);
            threadEm.set(em);
        }
        return threadEm.get();
    }

    public static void inTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = JpaUtils.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            consumer.accept(em);
            em.flush();
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw ex;
        }
    }

    public static void withEntityManager(Consumer<EntityManager> consumer) {
        // it would be even better to use the try-with-resource pattern, but
        // sadly the EntityManager does not support this for Hibernate 5.x
        // Available since JPA 3.1 (Hibernate 6.2+): https://github.com/jakartaee/persistence/issues/77
        try {
            consumer.accept(JpaUtils.getEntityManager());
        } finally {
            EntityManager em = threadEm.get();
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
}
