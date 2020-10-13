import org.hibernate.Session;

import java.util.List;
import java.util.stream.Stream;

public class PurchaseController {

    private static PurchaseController instance;

    private PurchaseController () {

    }

    public static PurchaseController getInstance() {
        if (instance == null) {
            instance = new PurchaseController();
        }
        return instance;
    }

    Model model = Model.getInstance();

    public void savePurchase(Purchase purchase) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(purchase);
        session.flush();
        session.close();
    }

    //may be not needed
    public List<Purchase> getAllPurchases() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        // TODO: resolve unchecked assignment
        return session.createCriteria(Purchase.class).list();
    }
}
