import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PurchaseController {

    // TODO: why different controllers for every entity?

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

    public List<Purchase> getTodayPurchases() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Purchase.class);
        Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        // TODO: resolve unchecked assignment
        List<Purchase> result = (List<Purchase>) criteria.add(
                Restrictions.gt("timestamp",date)).list();
        return result;
    }
//    public void getSomething() {
//        Session session = HibernateUtil.getSessionFactory().openSession();
//        Criteria criteria = session.createCriteria(Purchase.class);
//    }
}
