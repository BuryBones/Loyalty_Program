import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    private static String username;
    private static String password;

    public static void setUsername(String username) {
        HibernateUtil.username = username;
    }
    public static void setPassword(String password) {
        HibernateUtil.password = password;
    }

    public static SessionFactory getSessionFactory() {
        // ask for username and password
        // TODO: make login work again!
        if (username == null && password == null) {
            UiController.getInstance().invokeLogin();
        }
        if (sessionFactory == null) {
            try {
                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
                Map<String,String> settings = new HashMap<>();
                settings.put(Environment.DRIVER,"org.postgresql.Driver");
                settings.put(Environment.URL,"jdbc:postgresql://localhost:5432/testdb");
                settings.put(Environment.USER,username);
                settings.put(Environment.PASS,password);
                settings.put(Environment.DIALECT,"org.hibernate.dialect.PostgreSQL9Dialect");

                registryBuilder.applySettings(settings);

                registry = registryBuilder.build();

                MetadataSources sources = new MetadataSources(registry);
                sources.addAnnotatedClass(Client.class);
                sources.addAnnotatedClass(Purchase.class);
                Metadata metadata =sources.getMetadataBuilder().build();

                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }
        return sessionFactory;
    }
    public static void close() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
