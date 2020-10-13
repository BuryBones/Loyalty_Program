import org.hibernate.Session;

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
}
