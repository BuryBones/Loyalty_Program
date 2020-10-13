import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class ClientController {

    private static ClientController instance;

    private ClientController() {

    }

    public static ClientController getInstance() {
        if (instance == null) {
            instance = new ClientController();
        }
        return instance;
    }

    Model model = Model.getInstance();

    public boolean findClient() {
        String phone = model.getPhone();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Client.class);
        Client result = (Client) criteria.add(Restrictions.eq("phone", phone))
                .uniqueResult();
        Model model = Model.getInstance();
        model.setClient(result);
        // for development usage
        System.out.println(result);
        //
        return result != null;
    }
    public void createClient() {
        // TODO: maybe ask model to create new client first?
        Client client = model.getCurrentClient();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(client);
        session.flush();
        session.close();
    }
    public void updateClient() {
        Client client = model.getCurrentClient();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.update(client);
        session.flush();
        session.close();
    }
}
